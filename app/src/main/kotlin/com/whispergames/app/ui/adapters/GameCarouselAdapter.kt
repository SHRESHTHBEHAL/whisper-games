package com.whispergames.app.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.shreshth.whispergames.R
import com.whispergames.app.ui.models.GameInfo

/**
 * Adapter for the game carousel ViewPager2
 */
class GameCarouselAdapter(
    private val games: List<GameInfo>,
    private val onGameClick: (GameInfo) -> Unit
) : RecyclerView.Adapter<GameCarouselAdapter.GameViewHolder>() {

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardGame: CardView = view.findViewById(R.id.cardGame)
        val cardBackground: ConstraintLayout = view.findViewById(R.id.cardBackground)
        val tvGameIcon: TextView = view.findViewById(R.id.tvGameIcon)
        val tvGameName: TextView = view.findViewById(R.id.tvGameName)
        val tvGameDescription: TextView = view.findViewById(R.id.tvGameDescription)
        val tvDifficulty: TextView = view.findViewById(R.id.tvDifficulty)
        val tvHighScore: TextView = view.findViewById(R.id.tvHighScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_card, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]

        holder.tvGameIcon.text = game.icon
        holder.tvGameName.text = game.name
        holder.tvGameDescription.text = game.description
        holder.tvDifficulty.text = game.difficulty
        holder.tvHighScore.text = "High Score: ${game.highScore}"

        // Set background color
        try {
            holder.cardBackground.setBackgroundColor(Color.parseColor(game.backgroundColor))
        } catch (e: Exception) {
            holder.cardBackground.setBackgroundColor(Color.parseColor("#6200EE"))
        }

        holder.cardGame.setOnClickListener {
            onGameClick(game)
        }
    }

    override fun getItemCount() = games.size
}
