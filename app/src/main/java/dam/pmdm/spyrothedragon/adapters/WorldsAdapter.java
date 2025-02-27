package dam.pmdm.spyrothedragon.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dam.pmdm.spyrothedragon.R;
import dam.pmdm.spyrothedragon.models.World;

/**
 * The type Worlds adapter.
 */
public class WorldsAdapter extends RecyclerView.Adapter<WorldsAdapter.WorldsViewHolder> {

    private List<World> list;

    /**
     * Instantiates a new Worlds adapter.
     *
     * @param worldsList the worlds list
     */
    public WorldsAdapter(List<World> worldsList) {
        this.list = worldsList;
    }

    @Override
    public WorldsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        return new WorldsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WorldsViewHolder holder, int position) {
        World world = list.get(position);
        holder.nameTextView.setText(world.getName());

        // Cargar la imagen (simulado con un recurso drawable)
        int imageResId = holder.itemView.getContext().getResources().getIdentifier(world.getImage(), "drawable", holder.itemView.getContext().getPackageName());
        holder.imageImageView.setImageResource(imageResId);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * The type Worlds view holder.
     */
    public static class WorldsViewHolder extends RecyclerView.ViewHolder {

        /**
         * The Name text view.
         */
        TextView nameTextView;
        /**
         * The Image image view.
         */
        ImageView imageImageView;

        /**
         * Instantiates a new Worlds view holder.
         *
         * @param itemView the item view
         */
        public WorldsViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            imageImageView = itemView.findViewById(R.id.image);
        }
    }
}
