package com.enigma.projectmtndew.services;

import com.enigma.projectmtndew.dtos.ExpenseDTO;
import com.enigma.projectmtndew.dtos.OcrReceiptResponseDTO;
import com.enigma.projectmtndew.dtos.ScannedReceiptExpenseRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class OcrService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    String receiptPrompt = """
        You are a receipt parser.
        Extract all line items from this receipt image.
        Return ONLY valid JSON. No explanation. No markdown. No backticks.
        Use exactly this structure:
        {
          "items": [
            {"name": "item name", "price": 0.00, "quantity": 1}
          ],
          "subTotal": 0.00,
          "tax": 0.00,
          "serviceCharge": 0.00,
          "total": 0.00
        }
        If tax or service charge not found use 0.00.
        Price should be per single item not total for quantity.
        """;

    public String ask(String prompt) throws  Exception {

        // build a req body
        String requestBody = """
               {
                "contents": [{
                    "parts": [{
                        "text": "%s"
                    }]
                }]
               }
                """.formatted(prompt);

        //build the req
        HttpRequest request = HttpRequest.newBuilder()
                .uri(
                        URI.create(
                                "https://generativelanguage.googleapis.com/v1beta/models/" +
                                        "gemini-3.5-flash:generateContent?key=" + geminiApiKey
                        )
                )
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Gemini error: " + response.body());
            throw new RuntimeException("Gemini API error: " + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());

        return root
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asString();
    }

    public OcrReceiptResponseDTO scanImage(MultipartFile imageFile) throws Exception {
        byte[] imagesBytes = imageFile.getBytes();
        String imageBase64 = Base64.getEncoder().encodeToString(imagesBytes);

//        String requestBody = """
//               {
//                "contents": [{
//                    "parts": [{
//                        "inline_data": {
//                            "mime_type": "%s",
//                            "data": "%s"
//                        }
//                    },
//                    {
//                        "text": "%s"
//                    }]
//                }]
//               }
//                """.formatted(imageFile.getContentType(), imageBase64, receiptPrompt);

        // use ObjectMapper to build the JSON — it handles all escaping for you
        ObjectNode root = mapper.createObjectNode();
        ArrayNode contents = mapper.createArrayNode();
        ObjectNode content = mapper.createObjectNode();
        ArrayNode parts = mapper.createArrayNode();

        // image part
        ObjectNode imagePart = mapper.createObjectNode();
        ObjectNode inlineData = mapper.createObjectNode();
        inlineData.put("mime_type", imageFile.getContentType());
        inlineData.put("data", imageBase64);
        imagePart.set("inline_data", inlineData);

        // text part
        ObjectNode textPart = mapper.createObjectNode();
        textPart.put("text", receiptPrompt);  // ObjectMapper escapes everything automatically

        parts.add(imagePart);
        parts.add(textPart);
        content.set("parts", parts);
        contents.add(content);
        root.set("contents", contents);

        String requestBody = mapper.writeValueAsString(root);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(
                        URI.create(
                                "https://generativelanguage.googleapis.com/v1beta/models/" +
                                        "gemini-2.5-flash:generateContent?key=" + geminiApiKey
                        )
                )
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Gemini error: " + response.body());
            throw new RuntimeException("Gemini API error: " + response.statusCode());
        }

        JsonNode rootRes = mapper.readTree(response.body());

        String requiredResponseString =  rootRes
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asString();

        String cleaned = requiredResponseString
                .replace("```json", "")
                .replace("```", "")
                .trim();

        OcrReceiptResponseDTO responseDTO = mapper.readValue(cleaned, OcrReceiptResponseDTO.class);
        return responseDTO;
    }

    public ExpenseDTO handleScannedReceiptExpenseRequest(ScannedReceiptExpenseRequestDTO request) {
        Map<UUID, Float> userItemTotals = new HashMap<>();
        float calculatedSubtotal = 0f;

        // create map form subTotal...no tax yet
        for (ScannedReceiptExpenseRequestDTO.ScannedItem item : request.getItems()) {
            float itemTotal = item.getPrice() * item.getQuantity();
            calculatedSubtotal += itemTotal;

            float sharePerPerson = itemTotal / item.getSharedBy().size();
            for (UUID userId : item.getSharedBy()) {
                userItemTotals.merge(userId, sharePerPerson, Float::sum);
            }
        }

        // proportional tax and fees
        float total = request.getTotal();
        float taxAndFees = total - calculatedSubtotal;
        final float finalSubtotal = calculatedSubtotal;

        // 3. distribute tax proportionally and compute final percentages
        List<ExpenseDTO.ExpenseSplitDTO> splits = userItemTotals.entrySet().stream()
                .map(entry -> {
                    float userItems = entry.getValue();
                    float proportion = userItems / finalSubtotal;
                    float userTax = proportion * taxAndFees;
                    float userFinalTotal = userItems + userTax;

                    float percent = (userFinalTotal / total) * 100f;

                    ExpenseDTO.ExpenseSplitDTO split = new ExpenseDTO.ExpenseSplitDTO();
                    split.setId(entry.getKey());
                    split.setPercent(percent);
                    return split;
                })
                .toList();

        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setGroupId(request.getGroupId());
        expenseDTO.setPaidBy(request.getPaidBy());
        expenseDTO.setAmount(total);
        expenseDTO.setDescription(request.getDescription());
        expenseDTO.setSplitType(ExpenseDTO.SplitType.PERCENTAGE);
        expenseDTO.setSplits(splits);

        return expenseDTO;
    }
}
