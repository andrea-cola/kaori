package com.kaori.kaori.BottomBarFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.R;

public class ProfileFragment extends Fragment {
    private String userName;
    private String userSurname;
    private String userEmail;
    private String userBirthday;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);

        TextView nameSurnameView = view.findViewById(R.id.name_surname);
        nameSurnameView.setText(userName + userSurname);
        return view;
    }

    // TODO: fare l'autenticazione
    private void getCurrentUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User user = documentSnapshot.toObject(User.class);
                    userEmail = user.getEmail();
                    userName = user.getName();
                    userSurname = user.getSurname();
                    userBirthday = user.getBirthday();
                }
            });

        } else {
            //TODO: utente non loggato ma non succederà mai, if-else temporaneo
        }
    }
}
