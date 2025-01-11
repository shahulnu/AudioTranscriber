package com.springai.audiotranscriber;

import java.io.File;
import java.io.IOException;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
public class TranscriptionContorller {

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    public TranscriptionContorller(@Value("${spring.ai.openai.api-key}")String apiKey) {

        var openAiAudioApi = new OpenAiAudioApi(apiKey);
        this.openAiAudioTranscriptionModel = new OpenAiAudioTranscriptionModel(openAiAudioApi);
    }
    
    @PostMapping("/api/transcribe")
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        
        File tempFile = File.createTempFile("audio", ".wav");
        file.transferTo(tempFile);

        var transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                                        .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                                        .withLanguage("en")
                                        .withTemperature(0f)
                                        .build();

        var audioFile = new FileSystemResource(tempFile);

        var audioTranscriptionPrompt = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        var response = openAiAudioTranscriptionModel.call(audioTranscriptionPrompt);

        tempFile.delete();

        return new ResponseEntity<>(response.getResult().getOutput(), HttpStatus.OK);
    }


    
}
