package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;
import ao.co.isptec.aplm.projetoanuncioloc.R;

public class SelectableKeyAdapter extends RecyclerView.Adapter<SelectableKeyAdapter.KeyViewHolder> {

    private Context context;
    private List<ProfileKey> keys;
    private OnKeyClickListener listener;

    public interface OnKeyClickListener {
        void onKeyClick(ProfileKey key);
    }

    public SelectableKeyAdapter(Context context, List<ProfileKey> keys) {
        this.context = context;
        this.keys = keys;
    }

    public void setOnKeyClickListener(OnKeyClickListener listener) {
        this.listener = listener;
    }

    public void updateKeys(List<ProfileKey> newKeys) {
        this.keys = newKeys;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selectable_key, parent, false);
        return new KeyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        ProfileKey key = keys.get(position);
        holder.bind(key);
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    class KeyViewHolder extends RecyclerView.ViewHolder {
        CardView cardKeyItem;
        TextView tvKeyIcon, tvKeyName, tvKeyValuesCount;

        public KeyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardKeyItem = itemView.findViewById(R.id.card_key_item);
            tvKeyIcon = itemView.findViewById(R.id.tv_key_icon);
            tvKeyName = itemView.findViewById(R.id.tv_key_name);
            tvKeyValuesCount = itemView.findViewById(R.id.tv_key_values_count);
        }

        public void bind(ProfileKey key) {
            tvKeyIcon.setText(key.getName().substring(0, 1).toUpperCase());
            tvKeyName.setText(key.getName());

            int count = key.getAvailableCount();
            tvKeyValuesCount.setText(context.getString(R.string.values_unit, count));

            cardKeyItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onKeyClick(key);
                }
            });
        }
    }
}
