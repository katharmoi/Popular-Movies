package com.kadirkertis.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this,LoginActivity.class));
            return;
        }
        FirebaseUser user = mAuth.getCurrentUser();
        Toolbar toolbar = (Toolbar) findViewById(R.id.user_toolbar);
        toolbar.setTitle("User Info");
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView userText = (TextView)findViewById(R.id.user_text);
        userText.setText("You logged in as :" +user.getEmail());
    }

    public void onLogOutClick(View view){
        mAuth.signOut();
        Toast.makeText(this,"Logged out succesfully", Toast.LENGTH_SHORT);
        startActivity(new Intent(this,MainActivity.class));
    }
}
