package jp.ac.anan_nct.TabletInterphone.Activity;

import jp.ac.anan_nct.TabletInterphone.R;
import jp.ac.anan_nct.TabletInterphone.SharedVariable;
import jp.ac.anan_nct.TabletInterphone.Activity.Inside.InsideConnectSettingActivity;
import jp.ac.anan_nct.TabletInterphone.Activity.Outside.OutsideConnectSettingActivity;
import jp.ac.anan_nct.TabletInterphone.Activity.Outside.SelectBusinessActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends Activity {
	
	private SharedVariable sh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sh = (SharedVariable) this.getApplication();
		
		if(sh.isOutside == true){
			sh.selectBusinessFlag = 0x00;
			startActivity(SelectBusinessActivity.class);
		}
		
		setContentView(R.layout.activity_start);

		Button insideButton = (Button)findViewById(R.id.insideButton);
		Button outsideButton = (Button)findViewById(R.id.outsideButton);

		insideButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(InsideConnectSettingActivity.class);
			}
		});

		outsideButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(OutsideConnectSettingActivity.class);
			}
		});
		

	}
	
	private void startActivity(Class<?> cls){
		startActivity(new Intent(this, cls));
		finish();
	}
}
