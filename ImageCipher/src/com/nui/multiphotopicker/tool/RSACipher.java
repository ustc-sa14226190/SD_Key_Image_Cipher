package com.nui.multiphotopicker.tool;

import android.util.Base64;
import android.widget.Toast;

import com.haitaichina.p11.*;

public class RSACipher {

	public static Wrap11 p11;
	public static int slotId = 0;
	public static long[] keypair;
	public static CKA[] pub;
	public static CKA[] priv;

	public static boolean init() {
		p11 = new Wrap11();
		p11.throwCkrLine = false;
		p11.Initialize(slotId);
		if (!OpenPresentSession()) {
			if (CKR.TOKEN_NOT_PRESENT == p11.rv) {
				System.out.println("没有找到密码设备");
				return false;
			} else if (CKR.TOKEN_NOT_RECOGNIZED == p11.rv) {
				System.out.println("不能识别密码设备");
				return false;
			} else {
				System.out.println("不能打开密码设备");
				return false;
			}
		}
		if (!p11.isLoggedIn()) {
			p11.LoginUser(new String("111111"));
			if (0 != p11.rv) {
				System.out.println("登陆失败");
				return false;
			}
		}
		pub = new CKA[] { new CKA(CKA.CLASS, CKO.PUBLIC_KEY),
				new CKA(CKA.KEY_TYPE, CKK.RSA), new CKA(CKA.TOKEN, true),
				new CKA(CKA.LABEL, "publabel"), new CKA(CKA.WRAP, true),
				new CKA(CKA.ENCRYPT, true) };
		priv = new CKA[] { new CKA(CKA.CLASS, CKO.PRIVATE_KEY),
				new CKA(CKA.KEY_TYPE, CKK.RSA), new CKA(CKA.TOKEN, true),
				new CKA(CKA.LABEL, "privlabel"), new CKA(CKA.DECRYPT, true), };
		generateKey();
		return true;
	}

	public static boolean generateKey() {
		keypair = new long[2];
		if (searchKey()) {
			return true;
		}
		p11.FindObjectsInit(pub);
		long[] pubs = p11.FindObjects();
		if (pubs.length != 0) {
			for (int i = 0; i < pubs.length; ++i) {
				p11.DestroyObject(pubs[i]);
			}
		}
		p11.FindObjectsFinal();
		p11.FindObjectsInit(priv);
		long[] privs = p11.FindObjects();
		if (privs.length != 0) {
			for (int i = 0; i < pubs.length; ++i) {
				p11.DestroyObject(privs[i]);
			}
		}
		p11.FindObjectsFinal();

		keypair = p11.GenerateKeyPair(new Mechanism(CKM.RSA_PKCS_KEY_PAIR_GEN),
				pub, priv);
		return true;
	}

	public static boolean searchKey() {
		p11.FindObjectsInit(pub);
		long[] pubs = p11.FindObjects();
		if (pubs.length == 0) {
			return false;
		} else if (pubs.length == 1) {
			keypair[0] = pubs[0];
		} else {
			return false;
		}
		p11.FindObjectsFinal();
		p11.FindObjectsInit(priv);
		long[] privs = p11.FindObjects();
		if (privs.length == 0) {
			return false;
		} else if (privs.length == 1) {
			keypair[1] = privs[0];
		} else {
			return false;
		}
		p11.FindObjectsFinal();
		return true;
	}

	public static String encrypt(String src) {
		if (!p11.isLoggedIn()) {
			p11.LoginUser("111111");
			if (0 != p11.rv) {
				System.out.println("登陆失败");
				return null;
			}
		}
		if(keypair == null)
		{
			generateKey();
		}
		byte[] cipher = p11.Encrypt(new Mechanism(CKM.RSA_PKCS), keypair[0],
				src.getBytes());
		String str = Base64.encodeToString(cipher, Base64.DEFAULT);
		return str;
	}

	public static String decrypt(String encrypted) {
		if (!p11.isLoggedIn()) {
			p11.LoginUser("111111");
			if (0 != p11.rv) {
				System.out.println("登陆失败");
				return null;
			}
		}
		if(keypair == null)
		{
			return null;
		}
		byte[] plain = null;
		try {
			plain = p11.Decrypt(new Mechanism(CKM.RSA_PKCS), keypair[1],
					Base64.decode(encrypted, Base64.DEFAULT));
		} catch (Exception ex) {
			plain = null;
			return null;
		}
		return new String(plain);
	}

	public static boolean OpenPresentSession() {
		if (p11.getOpenSlotId() == slotId) {
			return true;
		}
		p11.throwCkrLine = false;
		p11.OpenSession(slotId);
		return (CKR.OK == p11.rv);
	}
}
