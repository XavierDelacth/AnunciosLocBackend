package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;
import ao.co.isptec.aplm.projetoanuncioloc.R;

public class LocalAdapterTodosLocais extends RecyclerView.Adapter<LocalAdapterTodosLocais.LocalViewHolder> {

    private List<Local> listaLocais;
    private OnLocalClickListener listener;

    public interface OnLocalClickListener {
        void onLocalClick(Local local, int position);
    }

    public LocalAdapterTodosLocais(List<Local> listaLocais) {
        this.listaLocais = listaLocais;
    }

    public void setOnLocalClickListener(OnLocalClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todos_locais, parent, false);
        return new LocalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalViewHolder holder, int position) {
        Local local = listaLocais.get(position);
        holder.bind(local, position);
    }

    @Override
    public int getItemCount() {
        return listaLocais.size();
    }

    class LocalViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvRaio, tvLat, tvLng, tvTipo;

        public LocalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_nome_local);
            tvRaio = itemView.findViewById(R.id.tv_raio);
            tvLat = itemView.findViewById(R.id.tv_lat);
            tvLng = itemView.findViewById(R.id.tv_lng);
            tvTipo = itemView.findViewById(R.id.tv_tipo);
        }

        public void bind(Local local, int position) {
            tvNome.setText(local.getNome());

            boolean isWifi = local.getWifiIds() != null && !local.getWifiIds().isEmpty();

            if (isWifi) {
                // Local WiFi
                tvRaio.setVisibility(View.GONE);
                tvLat.setVisibility(View.GONE);
                tvLng.setVisibility(View.GONE);
                tvTipo.setText("WiFi");
                tvTipo.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else {
                // Local GPS
                tvRaio.setVisibility(View.VISIBLE);
                tvLat.setVisibility(View.VISIBLE);
                tvLng.setVisibility(View.VISIBLE);

                tvRaio.setText(String.format(Locale.getDefault(), "Raio: %dm | ", local.getRaio()));
                tvLat.setText(String.format(Locale.US, "Lat: %.4f | ", local.getLatitude()));
                tvLng.setText(String.format(Locale.US, "Lng: %.4f | ", local.getLongitude()));
                tvTipo.setText("GPS");
                tvTipo.setTextColor(itemView.getContext().getColor(R.color.verde_principal));
            }

            // Click no item inteiro
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocalClick(local, position);
                }
            });
        }
    }
}