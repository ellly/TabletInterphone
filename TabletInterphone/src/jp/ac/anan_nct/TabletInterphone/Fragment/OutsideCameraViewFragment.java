package jp.ac.anan_nct.TabletInterphone.Fragment;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import jp.ac.anan_nct.TabletInterphone.Const;
import jp.ac.anan_nct.TabletInterphone.R;
import jp.ac.anan_nct.TabletInterphone.SharedVariable;
import jp.ac.anan_nct.TabletInterphone.Activity.Outside.SelectBusinessActivity;
import jp.ac.anan_nct.TabletInterphone.Utility.Util;

public class OutsideCameraViewFragment extends BaseFragment{
	
	//0511�ǉ�
	private Timer waitTimer;					//�^�C�}�[�p
	private MainTimerTask waitTimerTask;		//�^�C�}�^�X�N�N���X
	private int count = 0;
	private int endflag = 0;
	Handler m_handler = null;
	private int TurnTime = 0;
	private int HTurnTime = 0;
	private SharedVariable sh;

	//
	
	@Override
	protected void onCreate(View view){
		//0831�ǉ�
		sharedVariable.cameraViewOutside = (ImageView)view.findViewById(R.id.cameraView);
		//0511�ǉ�
		m_handler = new Handler();
		this.waitTimer = new Timer();
		//�^�X�N�N���X�C���X�^���X����
		this.waitTimerTask = new MainTimerTask();				
		//�^�C�}�[�X�P�W���[���ݒ聕�J�n
		this.waitTimer.schedule(waitTimerTask, 200,200);
		//
			
	}
	
	protected class MainTimerTask extends TimerTask {
		
	@Override
		public void run() {
				m_handler.post(new Runnable() {
	                @Override
	                public void run() {
	        			final ImageView cameraViewOutside = sharedVariable.cameraViewOutside;
	                	cameraViewOutside.setImageBitmap(sharedVariable.getoutBm());
	                }
	            });
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		waitTimer.cancel();	
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_camera_view;
	}
}