package com.example.firebasephonenumber;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText mEditTextPhone, mEditTextCode;
    private Button mBtnGetCode;
    private Button mBtnVerifyCode;
    String mVerificationCode;




    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.

        mAuth = FirebaseAuth.getInstance();

        userIsLoggedIn();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextPhone = findViewById(R.id.phoneNumber_text);
        mEditTextCode = findViewById(R.id.code_text);

        mBtnGetCode = findViewById(R.id.get_code_button);
        mBtnVerifyCode = findViewById(R.id.verify_code_button);

        mBtnGetCode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

               sendVerificationCode();
            }
        });

        /*Verify Code and Sign In */

        mBtnVerifyCode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               verifySignInCode();
            }
        });


    }

    private void verifySignInCode() {
        Log.i("Code","Inside verifySignInCode");
        String code = mEditTextCode.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationCode, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void sendVerificationCode() {
        Log.i("NoCode","Inside sendVerificationCode");

        String phone = mEditTextPhone.getText().toString();

        if(phone.isEmpty())
        {
            mEditTextPhone.setError("Phone Number Is required");
            mEditTextPhone.requestFocus();
            return;
        }
        if (phone.length() < 10)
        {
            mEditTextPhone.setError("Please enter valid phone number");
            mEditTextPhone.requestFocus();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Log.i("NoCode","Inside onVerificationCompleted");

                /*if(phoneAuthCredential != null)
                Log.i("NoCode","Inside onVerificationCompleted phoneAuthCredential"+phoneAuthCredential.toString());

                signInWithPhoneAuthCredential(phoneAuthCredential);*/
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.i("Verify","onVerificationFailed Exception-> "+e.toString());
        }

        @Override
        public void onCodeSent(@NonNull String code, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(code, forceResendingToken);
            mVerificationCode = code;
            Log.i("Code","mVerificationId"+ mVerificationCode);

        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(getApplicationContext(),"Sign In successful",Toast.LENGTH_SHORT).show();

                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            if (user != null) {
                                final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                                //listener
                                mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()) {
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("phone", user.getPhoneNumber());
                                            userMap.put("name", user.getDisplayName());
                                            mUserDB.updateChildren(userMap);
                                        }

                                        userIsLoggedIn();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });

                            }



                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(getApplicationContext(),"Incorrect verification code",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void userIsLoggedIn() {
        Log.i("User","Inside userIsLoggedIn");

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
            finish();
            return;
        }
        else
            Log.i("User","User is NULL");
    }

}
