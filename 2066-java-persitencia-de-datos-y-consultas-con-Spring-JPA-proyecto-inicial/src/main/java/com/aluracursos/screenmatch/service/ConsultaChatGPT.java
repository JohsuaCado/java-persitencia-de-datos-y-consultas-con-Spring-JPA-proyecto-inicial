package com.aluracursos.screenmatch.service;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
public class ConsultaChatGPT {

        public static String obtenerTraduccion(String texto) {
            OpenAiService service = new OpenAiService("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");

            CompletionRequest requisicion = CompletionRequest.builder()
                    .model("gpt-3.5-turbo-instruct")
                    .prompt("traduce a español el siguiente texto: " + texto)
                    .maxTokens(1000)
                    .temperature(0.7)
                    .build();

            var respuesta = service.createCompletion(requisicion);
            return respuesta.getChoices().get(0).getText();
        }
}
