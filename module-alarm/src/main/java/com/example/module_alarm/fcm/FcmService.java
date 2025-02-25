package com.example.module_alarm.fcm;

import com.example.module_utility.response.Response;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FcmService {
    private final FcmRepository fcmRepository;

    @PostConstruct
    public void initFirebase() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("module-alarm/src/main/resources/serviceAccountKey.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(options);
    }

    public Response<Void> saveToken(FcmTokenDTO request){
        Optional<Fcm> existingFcm = fcmRepository.findByToken(request.getToken());
        if(existingFcm.isPresent()){
            return Response.error(409, "Token already exists");
        }
        Fcm fcm = request.toEntity();
        fcmRepository.save(fcm);
        return Response.successWithoutData();
    }


}
