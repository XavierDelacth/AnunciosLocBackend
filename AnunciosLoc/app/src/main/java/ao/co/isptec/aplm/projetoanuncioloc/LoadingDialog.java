package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class LoadingDialog extends Dialog {

    private TextView tvMensagem;

    public LoadingDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);
        setCancelable(false); // Não fecha ao tocar fora
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        tvMensagem = findViewById(R.id.tv_loading_message);
    }

    // Permite mudar a mensagem
    public void setMessage(String message) {
        if (tvMensagem != null) {
            tvMensagem.setText(message);
        }
    }

    @Override
    public void show() {
        if (!isShowing()) {
            super.show();
        }
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            super.dismiss();
        }
    }
}