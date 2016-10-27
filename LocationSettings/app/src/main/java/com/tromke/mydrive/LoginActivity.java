package com.tromke.mydrive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tromke.mydrive.util.ConnectionManager;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends Activity implements View.OnClickListener, TextView.OnEditorActionListener {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.tromke.mydrive.R.layout.activity_login);

        if (ParseUser.getCurrentUser().getUsername() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        // Set up the login form.
        mEmailView = (EditText) findViewById(com.tromke.mydrive.R.id.username);

        mPasswordView = (EditText) findViewById(com.tromke.mydrive.R.id.password);
             mPasswordView.setOnEditorActionListener(this);
        Button mLoginButton = (Button) findViewById(com.tromke.mydrive.R.id.sign_in_button);
        mLoginButton.setOnClickListener(this);

       // Button mRegisterButton = (Button) findViewById(R.id.register_button);
       // mRegisterButton.setOnClickListener(this);
        mProgressView = findViewById(com.tromke.mydrive.R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Password field is required");
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);

           if(ConnectionManager.getInstance(getApplicationContext()).isDeviceConnectedToInternet()){
               final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
               dialog.setMessage("Please wait");
               dialog.show();

               ParseUser.logInInBackground(email, password, new LogInCallback() {
                @Override
                public void done(final ParseUser parseUser, final ParseException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }

                            if (parseUser != null) {
                                // Hooray! The user is logged in.
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Signup failed. Look at the ParseException to see what happened.
                                Context context = getApplicationContext();
                                CharSequence text = "Error in authentication!";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, e.getMessage(), duration);
                                toast.show();
                            }
                        }
                    });
                }
            });
        }  else{
               Toast.makeText(getApplicationContext(),getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
           }
        }


    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_button:
                attemptLogin();
                break;
           // case R.id.register_button:
              //   registerUser();
            default: break;

        }
    }

    private void registerUser() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId ==R.id.password || actionId == EditorInfo.IME_NULL) {
            attemptLogin();
            return true;
        }
        return false;
    }
}



