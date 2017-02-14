package jp.ac.anan_nct.TabletInterphone.Activity.Outside;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import jp.ac.anan_nct.TabletInterphone.R;
import jp.ac.anan_nct.TabletInterphone.SharedVariable;
import jp.ac.anan_nct.TabletInterphone.Activity.StartActivity;
import jp.ac.anan_nct.TabletInterphone.Communication.WifiSocket;
import jp.ac.anan_nct.TabletInterphone.Dialog.CustomDialog;
import jp.ac.anan_nct.TabletInterphone.Dialog.DialogBuilder;
import jp.ac.anan_nct.TabletInterphone.Service.OutsideService;
import jp.ac.anan_nct.TabletInterphone.Utility.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.util.Log;

public class OutsideConnectByBlueToothActivity extends Activity {
	private static final Handler handler = new Handler();
	
	private Button wifiSettingButton;
	private Button startButton;
	
	private WifiSocket ws;
	private SharedVariable sh;
	

	private String ipAddressText;
	private int id1;
	private int id2;
	private int id3;
	private int id4;
	private long id0;
	private long id00;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sh = (SharedVariable) this.getApplication();
		sh.InitialImgArray();
		if(sh.isOutside == true){
			sh.selectBusinessFlag = 0x21;
			//Intent intent = new Intent(this, SelectBusinessActivity.class);
			//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//startActivity(intent);
			finish();
		}
		
		else{
		setContentView(R.layout.activity_outside_connect_setting);
		wifiSettingButton = (Button) findViewById(R.id.wifiSettingButton);
		startButton = (Button) findViewById(R.id.endPhoneCallButton);

		wifiSettingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showIpAddressDialog();
			}
		});

		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		wifiSettingButton.setVisibility(View.INVISIBLE);
		startButton.setVisibility(View.INVISIBLE);
		wifiSettingButton.setText("�I��");
		this.ipAddressText = ConfigRead();
		Log.d("idid","idid" + ipAddressText + "idid");

		id00 = Long.parseLong(ipAddressText);
		id1 = (int)(id00 / 1000000000l);
		id0 = id1 * 1000;
		id00 = Long.parseLong(ipAddressText);
		id00 = (id00 / 1000000l);
		id2 = (int)(id00 - id0);
		id0 = Long.parseLong(ipAddressText);
		id0 = (id0 / 1000l);
		id3 = (int)(id0 - (id00 * 1000l));
		id00 = Long.parseLong(ipAddressText);
		id4 = (int)(id00 - (id0 * 1000l));
		
		Log.d("idid","idid" + id1 + "idid");
		Log.d("idid","idid" + id2 + "idid");
		Log.d("idid","idid" + id3 + "idid");
		Log.d("idid","idid" + id4 + "idid");
		//this.ipAddressText = Util.getIpAddressText(192, 168, 111, 101);
		this.ipAddressText = Util.getIpAddressText(id1, id2, id3, id4);
		sh.selectBusinessFlag = 0x00;
		sh.sharedIPadressText = this.ipAddressText;
		startConnect();
		}
	}
	
	private String ConfigRead(){
		String adress = "0.0.0.0";
		
		String filePath = Environment.getExternalStorageDirectory() +"/TIConfig//TIConfig.txt";
		File file = new File(filePath);
		file.getParentFile().mkdir();
		
		FileInputStream fis;
		
		try{
			fis = new FileInputStream(file);
			byte[] readBytes = new byte[fis.available()];
			fis.read(readBytes);
			adress = new String(readBytes);
			fis.close();		
		}catch(IOException e){
		}
		
		Log.d("adress","adressOf" + adress);
		
		return adress;
		
	}
	
	

	/***************************
	 * Wi-Fi�̒ʐM�m��
	 ***************************/
	private void startConnect() {
		ws = new WifiSocket();
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("�ڑ���");
		progressDialog.setMessage("Wi-Fi�ڑ� : �ڑ���");
		progressDialog.setCancelable(true);
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "�L�����Z��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ws.stopConnect();
				showErrorDialog("Wi-Fi�ڑ��Ɏ��s���܂���");
			}
		});
		progressDialog.show();
		
		// Wi-Fi�ڑ��X���b�h
		new Thread(new Runnable() {
			@Override
			public void run() {
				// �ڑ�
				if (ws.connectToServer(ipAddressText)){
					Log.d("Ouc","El01" + ipAddressText);
					progressDialog.dismiss();
					Log.d("connect","connect01");
					handler.post(new Runnable(){
						@Override
						public void run(){
							startOutside();
						}
					});
				}
				else{
					progressDialog.dismiss();
					handler.post(new Runnable(){
						@Override
						public void run(){
							restartConnect();
						}
					});
				}
			}
		}).start();
	}
	
	private void restartConnect(){
		ws.stopConnect();
		Util.sleep(4000);
		startConnect();
	}
	
	private void startOutside(){
		SharedVariable sh = Util.getSharedVariable(this);
		sh.wifiSocket = this.ws;
		Log.d("OuC","El01");
		startService(new Intent(this, OutsideService.class));
		finish();
		/*
		new DialogBuilder(this)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle("�ݒ芮��")
		.setMessage("�ݒ肪�������܂����B")
		.setCanceledOnTouchOutside(false)
		.setOnDismissListener(new DialogBuilder.OnDismissListener(){
			@Override
			protected void onDismiss(CustomDialog dialog){
				finish();
			}
		})
		.setPositiveButton("OK", null)
		.show("�ݒ芮��");
		*/
	}

	/***************************
	 * IP�A�h���X�ݒ�_�C�A���O�\��
	 ***************************/
	private void showIpAddressDialog() {
		final View view = LayoutInflater.from(this).inflate(R.layout.dialog_ipaddress, null);
		final NumberPicker[] ipAddressPicker = new NumberPicker[4];
		ipAddressPicker[0] = (NumberPicker) view.findViewById(R.id.ipAddressPicker1);
		ipAddressPicker[1] = (NumberPicker) view.findViewById(R.id.ipAddressPicker2);
		ipAddressPicker[2] = (NumberPicker) view.findViewById(R.id.ipAddressPicker3);
		ipAddressPicker[3] = (NumberPicker) view.findViewById(R.id.ipAddressPicker4);
		for (int i = 0; i < ipAddressPicker.length; i++) {
			ipAddressPicker[i].setMaxValue(255);
			ipAddressPicker[i].setMinValue(0);
		}

		ipAddressPicker[0].setValue(192);
		ipAddressPicker[1].setValue(168);
		ipAddressPicker[2].setValue(111);
		ipAddressPicker[3].setValue(101);

		new DialogBuilder(this)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle("Wi-Fi�ڑ��ݒ�")
			.setView(view)
			.setNegativeButton("�L�����Z��", null)
			.setPositiveButton("����", new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int ip1 = ipAddressPicker[0].getValue();
					int ip2 = ipAddressPicker[1].getValue();
					int ip3 = ipAddressPicker[2].getValue();
					int ip4 = ipAddressPicker[3].getValue();
					ipAddressText = Util.getIpAddressText(ip1, ip2, ip3, ip4);
					sh.sharedIPadressText = ipAddressText;
					startButton.setEnabled(true);
					wifiSettingButton.setText("Wi-Fi�ݒ�\n\n" + ipAddressText);
				}
			}).show("Wi-Fi�ڑ��ݒ�");
	}
	
	/***************************
	 * �����L�[������
	 ***************************/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK){
			startActivity(new Intent(this, StartActivity.class));
			finish();
			return true;
		}
		else
			return super.onKeyDown(keyCode, event);
	}
	
	private void showErrorDialog(String message){
		DialogBuilder.showErrorDialog(this, message);
	}
}
