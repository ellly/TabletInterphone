package jp.ac.anan_nct.TabletInterphone.Activity.Outside;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

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

public class OutsideConnectSettingActivity extends Activity {
	private static final Handler handler = new Handler();
	
	private Button wifiSettingButton;
	private Button startButton;
	
	private WifiSocket ws;
	private SharedVariable sh;
	
	private String ipAddressText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_outside_connect_setting);

		wifiSettingButton = (Button) findViewById(R.id.wifiSettingButton);
		startButton = (Button) findViewById(R.id.endPhoneCallButton);
		sh = (SharedVariable) this.getApplication();
		startButton.setEnabled(true);

		wifiSettingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showIpAddressDialog(true);
			}
		});

		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//startConnect();
				quickstart();
			}
		});
	}
	
	private void quickstart(){
		startActivity(new Intent(this, OutsideConnectByBlueToothActivity.class));
		finish();
	}

	/***************************
	 * Wi-Fiの通信確立
	 ***************************/
	private void startConnect() {
		ws = new WifiSocket();
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("接続中");
		progressDialog.setMessage("Wi-Fi接続 : 接続中");
		progressDialog.setCancelable(true);
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ws.stopConnect();
				showErrorDialog("Wi-Fi接続に失敗しました");
			}
		});
		progressDialog.show();
		
		// Wi-Fi接続スレッド
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 接続
				if (ws.connectToServer(ipAddressText)){
					Log.d("Ouc","El01" + ipAddressText);
					progressDialog.dismiss();
					handler.post(new Runnable(){
						@Override
						public void run(){
							startOutside();
						}
					});
				}
				else{
					progressDialog.cancel();
				}
			}
		}).start();
	}
	
	private void startOutside(){
		SharedVariable sh = Util.getSharedVariable(this);
		sh.wifiSocket = this.ws;
		Log.d("OuC","El01");
		startService(new Intent(this, OutsideService.class));

		new DialogBuilder(this)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle("設定完了")
		.setMessage("設定が完了しました。")
		.setCanceledOnTouchOutside(false)
		.setOnDismissListener(new DialogBuilder.OnDismissListener(){
			@Override
			protected void onDismiss(CustomDialog dialog){
				finish();
			}
		})
		.setPositiveButton("OK", null)
		.show("設定完了");
	}

	private void showIpAddressDialog(boolean b) {
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
			.setTitle("Wi-Fi接続設定")
			.setView(view)
			.setNegativeButton("キャンセル", null)
			.setPositiveButton("決定", new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int ip1 = ipAddressPicker[0].getValue();
					int ip2 = ipAddressPicker[1].getValue();
					int ip3 = ipAddressPicker[2].getValue();
					int ip4 = ipAddressPicker[3].getValue();
					writeBusiness(String.valueOf(ip1) + String.valueOf(ip2) + String.valueOf(ip3) + String.valueOf(ip4));
				}
			}).show("Wi-Fi接続設定");
	}
	
	private void writeBusiness(String ipAddressText){
		//OutputStream out;
		
		String filePath = Environment.getExternalStorageDirectory() + "/TIConfig/TIConfig.txt";
		File file = new File(filePath);
		file.getParentFile().mkdir();
		
		FileOutputStream fos;
		
		try{
			fos = new FileOutputStream(file, false);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(ipAddressText);
			bw.flush();
			bw.close();
			
		}catch(Exception e){
			Log.d("Vic","Vic" + e);
		}
	}
	
	/***************************
	 * IPアドレス設定ダイアログ表示
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
			.setTitle("Wi-Fi接続設定")
			.setView(view)
			.setNegativeButton("キャンセル", null)
			.setPositiveButton("決定", new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int ip1 = ipAddressPicker[0].getValue();
					int ip2 = ipAddressPicker[1].getValue();
					int ip3 = ipAddressPicker[2].getValue();
					int ip4 = ipAddressPicker[3].getValue();
					ipAddressText = Util.getIpAddressText(ip1, ip2, ip3, ip4);
					sh.sharedIPadressText = ipAddressText;
					sh.InitialImgArray();
					startButton.setEnabled(true);
					wifiSettingButton.setText("Wi-Fi設定\n\n" + ipAddressText);
				}
			}).show("Wi-Fi接続設定");
	}
	
	/***************************
	 * 物理キー押下時
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
