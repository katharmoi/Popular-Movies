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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout mEmailLayout;
    private TextInputLayout mPasswordLayout;
    private Button mLoginBtn;
    private Toolbar mToolBar;
    private TextView mSignUpTextView;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mFirebaseAuth = FirebaseAuth.getInstance();
        if(mFirebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(this,MainActivity.class));
        }
        mToolBar = (Toolbar)findViewById(R.id.login_toolbar);
        mToolBar.setTitle(R.string.login_title);
        mToolBar.setNavigationIcon(ContextCompat.getDrawable(this,R.drawable.ic_action_arrow_left));
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mEmailLayout = (TextInputLayout)findViewById(R.id.login_email_layout);
        mPasswordLayout = (TextInputLayout)findViewById(R.id.login_pass_layout);
        mLoginBtn = (Button)findViewById(R.id.login_btn);
        mSignUpTextView = (TextView) findViewById(R.id.sign_up_link_text);
        mLoginBtn.setOnClickListener(this);
        mSignUpTextView.setOnClickListener(this);
        mProgressDialog = new ProgressDialog(this);

    }

    @Override
    public void onClick(View view) {
        if(view == mLoginBtn){
            signInUser();
        }
        if(view == mSignUpTextView){
            startActivity(new Intent(this,SignUpActivity.class));
        }
        if(view == mToolBar){
            startActivity(new Intent(this,MainActivity.class));
        }

    }

    private void signInUser(){
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

        mProgressDialog.setMessage("Please wait while logging in...");
        mProgressDialog.show();

        mFirebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,"Logged In Successfully",Toast.LENGTH_SHORT).show();
                            finish();
                            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }else{
                            Toast.makeText(LoginActivity.this,"Wrong password or email.Please try again",
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });




    }
}
