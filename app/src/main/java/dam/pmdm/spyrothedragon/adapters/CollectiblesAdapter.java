package dam.pmdm.spyrothedragon.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dam.pmdm.spyrothedragon.R;
import dam.pmdm.spyrothedragon.models.Collectible;

/**
 * The type Collectibles adapter.
 */
public class CollectiblesAdapter extends RecyclerView.Adapter<CollectiblesAdapter.CollectiblesViewHolder> {

    private List<Collectible> list;

    /**
     * Instantiates a new Collectibles adapter.
     *
     * @param collectibleList the collectible list
     */
    public CollectiblesAdapter(List<Collectible> collectibleList) {
        this.list = collectibleList;
    }

    @Override
    public CollectiblesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        return new CollectiblesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CollectiblesViewHolder holder, int position) {
        Collectible collectible = list.get(position);
        holder.nameTextView.setText(collectible.getName());

        // Cargar la imagen (simulado con un recurso drawable)
        int imageResId = holder.itemView.getContext().getResources().getIdentifier(collectible.getImage(), "drawable", holder.itemView.getContext().getPackageName());
        holder.imageImageView.setImageResource(imageResId);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * The type Collectibles view holder.
     */
    public static class CollectiblesViewHolder extends RecyclerView.ViewHolder {

        /**
         * The Name text view.
         */
        TextView nameTextView;
        /**
         * The Image image view.
         */
        ImageView imageImageView;

        /**
         * Instantiates a new Collectibles view holder.
         *
         * @param itemView the item view
         */
        public CollectiblesViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            imageImageView = itemView.findViewById(R.id.image);
        }
    }
}
