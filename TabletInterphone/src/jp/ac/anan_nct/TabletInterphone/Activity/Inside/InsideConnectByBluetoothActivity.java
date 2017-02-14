package jp.ac.anan_nct.TabletInterphone.Activity.Inside;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import jp.ac.anan_nct.TabletInterphone.R;
import jp.ac.anan_nct.TabletInterphone.SharedVariable;
import jp.ac.anan_nct.TabletInterphone.Activity.StartActivity;
import jp.ac.anan_nct.TabletInterphone.Activity.Outside.SelectBusinessActivity;
import jp.ac.anan_nct.TabletInterphone.Activity.Outside.WhoActivity;
import jp.ac.anan_nct.TabletInterphone.Activity.Inside.VisiterLogActivity;
import jp.ac.anan_nct.TabletInterphone.Communication.BluetoothConnection;
import jp.ac.anan_nct.TabletInterphone.Communication.WifiSocket;
import jp.ac.anan_nct.TabletInterphone.Dialog.CustomDialog;
import jp.ac.anan_nct.TabletInterphone.Dialog.DialogBuilder;
import jp.ac.anan_nct.TabletInterphone.Service.InsideService;
import jp.ac.anan_nct.TabletInterphone.Utility.BluetoothUtil;
import jp.ac.anan_nct.TabletInterphone.Utility.Util;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.util.Log;
import android.os.PowerManager;
import android.provider.Settings;
import android.app.KeyguardManager;


public class InsideConnectByBluetoothActivity extends Activity {
	private static final Handler handler = new Handler();
	
	private Button bluetoothSettingButton;
	private Button startButton;
	
	private String ipAddressText;
	private BluetoothStatus bluetoothStatus;
	private WifiStatus wifiStatus;
	
	private SharedVariable sh;

	private BluetoothUtil bu;
	private BluetoothDevice targetDevice = null;

	private BluetoothConnection bc;
	private WifiSocket ws = new WifiSocket();
	
	private enum BluetoothStatus{
		ERROR("Bluetooth�ڑ��Ɏ��s���܂���"),
		CONNECTING("Bluetooth�ڑ� : �ڑ���"),
		CONNECTED("Bluetooth�ڑ� : OK");
				
		private String message;
		
		private BluetoothStatus(String message){
			this.message = message;
		}
		
		public String toString(){
			return this.message;
		}
	}
	
	private enum WifiStatus{
		ERROR("Wi-Fi�ڑ��Ɏ��s���܂���"),
		CONNECTING("Wi-Fi�ڑ� : �ڑ��ҋ@��"),
		CONNECTED("Wi-Fi�ڑ� : OK");
				
		private String message;
		
		private WifiStatus(String message){
			this.message = message;
		}
		
		public String toString(){
			return this.message;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inside_connect_setting);
		
		sh = (SharedVariable) this.getApplication();
		
		if(sh.vcFlag){
			startActivity(new Intent(this,VisiterCheckActivity.class));
			finish();
		}
		
		else{
		bluetoothSettingButton = (Button) findViewById(R.id.bluetoothSettingButton);
		startButton = (Button) findViewById(R.id.endPhoneCallButton);

		bluetoothSettingButton.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				/////////////////////////////////////////////////////////////////////
				//�^�u���b�g�C���^�[�z���z�[���A�v��������//////////////////////////////////////*
				Intent intent = new Intent();
			    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			    intent.setData(Uri.parse("package:jp.ac.anan_nct.TabletInterphone"));
			    startActivity(intent);
			    //*//////////////////////////////////////////////////////////////////
			    ////////////////////////////////////////////////////////////////////
			    
			    //////////////////////////////////////////////////////////////////
			    //���O�\��/////////////////////////////////////////////////////////
				/*
				MoveLog();
			    //*////////////////////////////////////////////////////////////////
			    
				//4/21�ǉ�////5/25�R�����g�A�E�g
				//startButton.setEnabled(true);
				//bluetoothStatus = BluetoothStatus.CONNECTED;
				//wifiStatus = WifiStatus.CONNECTED;
				//startInside();		
				//
				
				/*4/21�R�����g�A�E�g�e�X�g2
				showBluetoothSelectDialog();
				*/
			}
		});

		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startConnect();
			}
		});
		bluetoothSettingButton.setVisibility(View.INVISIBLE);
		startButton.setVisibility(View.INVISIBLE);
		Log.d("LOGICRA","LOGICRA01");
		startConnect();
		}
	}
	
	//////////////////////////
	//0607�ǉ�				//
	//���O�\�����\�b�h			//
	//////////////////////////
	private void MoveLog(){
		startActivity(new Intent(this,WhoActivity.class));
		finish();
	}
	///////////////////////
	
	//////////////////////////////////////////////////////////
	//0526�ǉ�												//
	//�^�u���b�g�X���[�v�������\�b�h									//
	//���΂炭�҂��Ȃ��Ƌ@�\���Ȃ��usleep(700)�v����!					//
	//////////////////////////////////////////////////////////
	/*
	private void Wake(){
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		PowerManager.WakeLock wakelock;
		wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
		        | PowerManager.ACQUIRE_CAUSES_WAKEUP
		        | PowerManager.ON_AFTER_RELEASE, "Tablet Interphone");
		wakelock.acquire();
		KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock klock = km.newKeyguardLock("Tablet Interphone");
		klock.disableKeyguard();
		Util.sleep(700);
		wakelock.release();
		klock.reenableKeyguard();
	}
	*/

	/***************************
	 * Bluetooth��Wi-Fi�̒ʐM�m��
	 ***************************/
	private void startConnect() {
		ipAddressText = "�v���C�x�[�gIP�A�h���X : " + getIpAddress();
		bluetoothStatus = BluetoothStatus.CONNECTING;
		wifiStatus = WifiStatus.CONNECTING;
		
		bc = new BluetoothConnection();
		ws = new WifiSocket();

		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("�ڑ���");
		progressDialog.setMessage(ipAddressText + "\n\n" + bluetoothStatus.toString() + "\n" + wifiStatus.toString());
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
				if(ws != null){
					ws.stopServer();
					ws = null;
				}
				
				if(bc != null){
					bc.close();
					bc = null;
				}
			}
		});
		progressDialog.show();

		// Bluetooth�ڑ��X���b�h
		new Thread(new Runnable() {
			@Override
			public void run() {
				// �ڑ�
				/*4/21 �R�����g�A�E�g
				
				while(progressDialog.isShowing() && bluetoothStatus != BluetoothStatus.CONNECTED){
					bluetoothStatus = bc.connectToServer(targetDevice) ? BluetoothStatus.CONNECTED : BluetoothStatus.CONNECTING;
					
					if(bluetoothStatus == BluetoothStatus.CONNECTED)
						refreshProgressMessage(progressDialog);
					else
						Util.sleep(2000);
				}
				*/
			}
			
		}).start();
		
		// Wi-Fi�ڑ��X���b�h
		new Thread(new Runnable() {
			@Override
			public void run() {
				// �ڑ��ҋ@
				wifiStatus = ws.makeServer() ? WifiStatus.CONNECTED : WifiStatus.ERROR;
				
				refreshProgressMessage(progressDialog);
			}
		}).start();
	}
	
	private void refreshProgressMessage(final ProgressDialog dialog){
		//Wake();
		handler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (dialog) {
					if(!dialog.isShowing()){
						
					}
					else if(/*bluetoothStatus == BluetoothStatus.CONNECTED 4/22�R�����g�A�E�g&& */wifiStatus == WifiStatus.CONNECTED){ //�����ڑ�����
						dialog.dismiss();
						startInside();
					}
					else if(bluetoothStatus == BluetoothStatus.ERROR){ //Bluetooth�ڑ��G���[
						dialog.cancel();
						showErrorDialog(bluetoothStatus.toString());
					}
					else if(wifiStatus == WifiStatus.ERROR){ //Wi-Fi�ڑ��G���[
						dialog.cancel();
						showErrorDialog(wifiStatus.toString());
					}
					else{ //�ڑ���
						dialog.setMessage(ipAddressText + "\n\n" + bluetoothStatus.toString() + "\n" + wifiStatus.toString());
					}
				}
			}
		});
	}
	
	private void startInside(){
		SharedVariable sh = Util.getSharedVariable(this);
		sh.initialRingtone(this);
		sh.wifiSocket = this.ws;
		sh.bluetoothConnection = this.bc;
		startService(new Intent(this, InsideService.class));
		
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
	 * �v���C�x�[�gIP�A�h���X�擾
	 *
	 * @return ���g�̃v���C�x�[�gIP�A�h���X��Ԃ��B
	 ***************************/
	private String getIpAddress() {
		WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		
		// IP�A�h���X���擾
		int ipAdr = info.getIpAddress();
		return Util.getIpAddressText(ipAdr & 0xff, (ipAdr >> 8) & 0xff, (ipAdr >> 16) & 0xff, (ipAdr >> 24) & 0xff);
	}

	/***************************
	 * Bluetooth�f�o�C�X�I��
	 ***************************/
	private void showBluetoothSelectDialog() {
		this.bu = new BluetoothUtil();
		
		if (!this.bu.isSpported()) // ��Ή��f�o�C�X
			DialogBuilder.showErrorDialog(this, "Bluetooth��Ή��f�o�C�X�ł��B");
		else if (!this.bu.isEnabled()) // �ݒ薳��
			DialogBuilder.showErrorDialog(this, "Bluetooth�L���ɂ��Ă��������B");
		else if (this.bu.getPairingCount() == 0) // �y�A�����O�ς݃f�o�C�X�Ȃ�
			DialogBuilder.showErrorDialog(this, "�y�A�����O�ς݂�Bluetooth�ݒ肪����܂���B");
		else {
			new DialogBuilder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("Bluetooth�f�o�C�X�I��")
				.setItems(bu.getDeviceNames(), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						decideBluetoothDevice(bu.getDevices()[which]);
					}
				})
				.setNegativeButton("�L�����Z��", null)
				.show("Bluetooth�f�o�C�X�I��");
		}
	}

	/***************************
	 * Bluetooth�[������
	 * 
	 * @param bluetoothDevice �I������Bluetooth�[��
	 ***************************/
	private void decideBluetoothDevice(BluetoothDevice bluetoothDevice) {
		this.bluetoothSettingButton.setText("Bluetooth�ݒ�\n\n�Ώ� : " + bluetoothDevice.getName());
		this.targetDevice = bluetoothDevice;
		this.startButton.setEnabled(true);
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
