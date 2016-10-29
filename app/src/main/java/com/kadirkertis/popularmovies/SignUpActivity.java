package com.kadirkertis.popularmovies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout mEmailLayout;
    private TextInputLayout mPasswordLayout;
    private Button mSignUpBtn;
    private Toolbar mToolBar;
    private TextView mLoginTextView;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mFirebaseAuth = FirebaseAuth.getInstance();
        if(mFirebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(this,MainActivity.class));
        }
        mToolBar = (Toolbar)findViewById(R.id.sign_up_toolbar);
        mToolBar.setTitle(R.string.sign_up_title);
        mToolBar.setNavigationIcon(ContextCompat.getDrawable(this,R.drawable.ic_action_arrow_left));
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mEmailLayout = (TextInputLayout)findViewById(R.id.sign_up_email_layout);
        mPasswordLayout = (TextInputLayout)findViewById(R.id.sign_up_pass_layout);
        mSignUpBtn = (Button)findViewById(R.id.sign_up_btn);
        mLoginTextView = (TextView) findViewById(R.id.login_link_text);
        mSignUpBtn.setOnClickListener(this);
        mLoginTextView.setOnClickListener(this);
        mProgressDialog = new ProgressDialog(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        if(view== mSignUpBtn){
            signUpUser();
        }
        if(view == mLoginTextView){
            startActivity(new Intent(this,LoginActivity.class));
        }
    }

    private void signUpUser(){
        String email =mEmailLayout.getEditText().getText().toString().trim();
        String pass = mPasswordLayout.getEditText().getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            mEmailLayout.setError("Email cannot be blank");
            return;
        }

        if(TextUtils.isEmpty(pass)){
            mPasswordLayout.setError("Password cannot be blank");
            return;
        }

        mProgressDialog.setMessage("Please wait while signing up...");
        mProgressDialog.show();

        mFirebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            finish();
                            Toast.makeText(SignUpActivity.this,"Signed Up Successfully",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(SignUpActivity.this,"Registration is not successful." +
                                    "Please make sure you entered a valid email"
                                    ,
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });




    }

}
