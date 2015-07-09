package com.nui.multiphotopicker.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.nui.multiphotopicker.R;
import com.nui.multiphotopicker.tool.RSACipher;

public class LoginActivity extends Activity {
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	private EditText accountEdit;
	private EditText passwordEdit;
	private Button login;
	private CheckBox rememberPass;
	private boolean flag_sdk;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		flag_sdk = true;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		try {
			if (!RSACipher.init()) {
				flag_sdk = false;
				Toast.makeText(getApplicationContext(), "无 SD Key，应用无法登录",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception ex) {
			flag_sdk = false;
		}
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		accountEdit = (EditText) findViewById(R.id.account);
		passwordEdit = (EditText) findViewById(R.id.password);
		rememberPass = (CheckBox) findViewById(R.id.remember_pass);
		login = (Button) findViewById(R.id.login);
		boolean isRemember = pref.getBoolean("remember_password", false);
		if (isRemember) {
			// 将账号和密码都设置到文本框中
			String account = pref.getString("account", "");
			String password = pref.getString("password", "");
			accountEdit.setText(account);
			passwordEdit.setText(password);
			rememberPass.setChecked(true);
		}
		login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!flag_sdk) {
					Toast.makeText(getApplicationContext(), "请插入SD Key",
							Toast.LENGTH_LONG).show();
					return;
				}
				String account = accountEdit.getText().toString();
				String password = passwordEdit.getText().toString();
				if (account.equals("admin") && password.equals("123456")) {
					editor = pref.edit();
					if (rememberPass.isChecked()) {
						editor.putBoolean("remember_password", true);
						editor.putString("account", account);
						editor.putString("password", password);
					} else {
						editor.clear();
					}
					editor.commit();
					Intent intent = new Intent(LoginActivity.this,
							PublishActivity.class);
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(LoginActivity.this,
							"account or password is invalid",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
