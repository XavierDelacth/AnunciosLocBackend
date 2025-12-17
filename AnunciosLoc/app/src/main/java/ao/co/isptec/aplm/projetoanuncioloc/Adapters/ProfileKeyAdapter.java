package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;
import ao.co.isptec.aplm.projetoanuncioloc.R;


public class ProfileKeyAdapter extends RecyclerView.Adapter<ProfileKeyAdapter.KeyViewHolder> {

    private Context context;
    private List<ProfileKey> keys;
    private boolean showOnlySelected;
    private OnValueClickListener listener;

    public interface OnValueClickListener {
        void onValueClick(String keyName, String value);
    }

    public ProfileKeyAdapter(Context context, List<ProfileKey> keys, boolean showOnlySelected) {
        this.context = context;
        this.keys = keys;
        this.showOnlySelected = showOnlySelected;
    }

    public void setOnValueClickListener(OnValueClickListener listener) {
        this.listener = listener;
    }

    public void setShowOnlySelected(boolean showOnlySelected) {
        this.showOnlySelected = showOnlySelected;
    }

    public void updateKeys(List<ProfileKey> newKeys) {
        this.keys = newKeys;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile_key, parent, false);
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
        CardView cardKeyIcon;
        TextView tvKeyIcon, tvKeyName, tvKeyInfo;
        ImageView imgExpand;
        LinearLayout layoutKeyHeader, layoutValuesContainer;
        FlexboxLayout flexboxValues;
        boolean isExpanded = false;

        public KeyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardKeyIcon = itemView.findViewById(R.id.card_key_icon);
            tvKeyIcon = itemView.findViewById(R.id.tv_key_icon);
            tvKeyName = itemView.findViewById(R.id.tv_key_name);
            tvKeyInfo = itemView.findViewById(R.id.tv_key_info);
            imgExpand = itemView.findViewById(R.id.img_expand);
            layoutKeyHeader = itemView.findViewById(R.id.layout_key_header);
            layoutValuesContainer = itemView.findViewById(R.id.layout_values_container);
            flexboxValues = itemView.findViewById(R.id.flexbox_values);
        }

        public void bind(ProfileKey key) {
            // Set key icon (first letter)
            tvKeyIcon.setText(key.getName().substring(0, 1).toUpperCase());
            tvKeyName.setText(key.getName());

            // Update icon background color based on selection
            boolean hasSelection = !key.getSelectedValues().isEmpty();
            if (hasSelection) {
                cardKeyIcon.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.green_500)
                );
            } else {
                cardKeyIcon.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.gray_300)
                );
            }

            // Set key info
            if (showOnlySelected) {
                // My Keys tab - show selected values
                String selectedInfo = String.join(", ", key.getSelectedValues());
                tvKeyInfo.setText(selectedInfo);
            } else {
                // Public Keys tab - show count
                int totalCount = key.getAvailableValues().size();
                int selectedCount = key.getSelectedValues().size();

                String info = totalCount + " valores disponíveis";
                if (selectedCount > 0) {
                    info += " • " + selectedCount + " selecionado(s)";
                }
                tvKeyInfo.setText(info);
            }

            // Header click listener
            layoutKeyHeader.setOnClickListener(v -> toggleExpanded());

            // Build values buttons
            buildValueButtons(key);
        }

        private void toggleExpanded() {
            isExpanded = !isExpanded;

            if (isExpanded) {
                layoutValuesContainer.setVisibility(View.VISIBLE);
                imgExpand.setRotation(180);
            } else {
                layoutValuesContainer.setVisibility(View.GONE);
                imgExpand.setRotation(0);
            }
        }

        private void buildValueButtons(ProfileKey key) {
            flexboxValues.removeAllViews();

            for (String value : key.getAvailableValues()) {
                Button btnValue = new Button(context);

                // Set layout params with margins
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 16, 12);
                btnValue.setLayoutParams(params);

                // Set button properties
                btnValue.setText(value);
                btnValue.setAllCaps(false);
                btnValue.setPadding(32, 20, 32, 20);
                btnValue.setTextSize(14);

                // Set style based on selection
                boolean isSelected = key.getSelectedValues().contains(value);
                btnValue.setSelected(isSelected);
                btnValue.setBackgroundResource(R.drawable.bg_value_button);

                if (isSelected) {
                    btnValue.setTextColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    btnValue.setTextColor(ContextCompat.getColor(context, R.color.gray_700));
                }

                // Click listener
                btnValue.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onValueClick(key.getName(), value);
                    }
                });

                flexboxValues.addView(btnValue);
            }
        }
    }
}