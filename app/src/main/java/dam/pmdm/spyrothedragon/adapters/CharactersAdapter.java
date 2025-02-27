package dam.pmdm.spyrothedragon.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import dam.pmdm.spyrothedragon.R;
import dam.pmdm.spyrothedragon.models.Character;

import java.util.List;

/**
 * The type Characters adapter.
 */
public class CharactersAdapter extends RecyclerView.Adapter<CharactersAdapter.CharactersViewHolder> {

    private List<Character> list;

    /**
     * Instantiates a new Characters adapter.
     *
     * @param charactersList the characters list
     */
    public CharactersAdapter(List<Character> charactersList) {
        this.list = charactersList;
    }

    @Override
    public CharactersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        return new CharactersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CharactersViewHolder holder, int position) {
        Character character = list.get(position);
        holder.nameTextView.setText(character.getName());

        // Cargar la imagen (simulado con un recurso drawable)
        int imageResId = holder.itemView.getContext().getResources().getIdentifier(character.getImage(), "drawable", holder.itemView.getContext().getPackageName());
        holder.imageImageView.setImageResource(imageResId);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * The type Characters view holder.
     */
    public static class CharactersViewHolder extends RecyclerView.ViewHolder {

        /**
         * The Name text view.
         */
        TextView nameTextView;
        /**
         * The Image image view.
         */
        ImageView imageImageView;

        /**
         * Instantiates a new Characters view holder.
         *
         * @param itemView the item view
         */
        public CharactersViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            imageImageView = itemView.findViewById(R.id.image);
        }
    }
}
