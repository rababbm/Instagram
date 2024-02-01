package com.example.instagram;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    NavController navController;
    public AppViewModel appViewModel;
    public PostRepository postRepository; // Declara postRepository

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //delete
        postRepository = new PostRepository(requireContext()); // Inicializa postRepository

        navController = Navigation.findNavController(view);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        view.findViewById(R.id.gotoNewPostFragmentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.newPostFragment);
            }
        });
        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        Query query = FirebaseFirestore.getInstance().collection("posts").orderBy("timeStamp", Query.Direction.DESCENDING).limit(50);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();

        postsRecyclerView.setAdapter(new PostsAdapter(options));
    }

    class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostViewHolder> {
        private PostRepository postRepository; //declaramos
        public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {
            super(options);
            this.postRepository = postRepository; // Inicialización de PostRepository
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull final Post post) {
            if (post.authorPhotoUrl == null) {
                holder.authorPhotoImageView.setImageResource(R.drawable.user);
            } else {
                Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(holder.authorPhotoImageView);
            }

            holder.authorTextView.setText(post.author);
            holder.contentTextView.setText(post.content);

            //gestion likes
            final String postKey = getSnapshots().getSnapshot(position).getId();
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (post.likes.containsKey(uid))
                holder.likeImageView.setImageResource(R.drawable.like_on);
            else
                holder.likeImageView.setImageResource(R.drawable.like_off);
            holder.numLikesTextView.setText(String.valueOf(post.likes.size()));
            holder.likeImageView.setOnClickListener(view -> {
                FirebaseFirestore.getInstance().collection("posts")
                        .document(postKey)
                        .update("likes." + uid, post.likes.containsKey(uid) ?
                                FieldValue.delete() : true);
            });
            // Miniatura de media
            if (post.mediaUrl != null) {
                holder.mediaImageView.setVisibility(View.VISIBLE);
                if ("audio".equals(post.mediaType)) {
                    Glide.with(requireView()).load(R.drawable.audio).centerCrop().into(holder.mediaImageView);
                } else {
                    Glide.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.mediaImageView);
                }
                holder.mediaImageView.setOnClickListener(view -> {
                    appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.mediaFragment);
                });
            } else {
                holder.mediaImageView.setVisibility(View.GONE);
            }
            //fecha y hora
            SimpleDateFormat formatter= new SimpleDateFormat("HH:MM dd/mm/yyyy");
            Calendar calendar= Calendar.getInstance();
            calendar.setTimeInMillis(post.timeStamp);

            holder.timeTextView.setText(formatter.format((calendar.getTime())));

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String postAuthorId = post.uid;

            if (currentUserId.equals(postAuthorId)) {
                // El usuario actual es el autor del post, mostrar el botón de eliminación
                holder.delete.setVisibility(View.VISIBLE);
                holder.edit.setVisibility(View.VISIBLE);

                holder.delete.setOnClickListener(view -> {
                    // Obtener el ID del post actual
                    String postId = getSnapshots().getSnapshot(position).getId();

                    // Llamar al método deletePost de postRepository para eliminar el post
                    HomeFragment.this.postRepository.deletePost(postId, currentUserId);
                });

            } else {
                // El usuario actual no es el autor del post, ocultar el botón de eliminación
                holder.delete.setVisibility(View.GONE);
                holder.edit.setVisibility(View.GONE);
            }
            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Obtener el contenido del post
                    String postContent = post.content;

                    // Crear un nuevo fragmento editPost con el contenido del post
                    Fragment editPostFragment = editPost.newInstance(postContent);
                    editPost.obtenerKey(postKey);
                    // Navegar al fragmento editPost utilizando el NavController
                    navController.navigate(R.id.action_homeFragment_to_editPost);
                }
            });


        }


        class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView authorPhotoImageView, likeImageView, mediaImageView,delete,edit;
            TextView authorTextView, contentTextView, numLikesTextView, timeTextView;


            PostViewHolder(@NonNull View itemView) {
                super(itemView);
                authorPhotoImageView = itemView.findViewById(R.id.photoImageView);
                likeImageView = itemView.findViewById(R.id.likeImageView);
                mediaImageView = itemView.findViewById(R.id.mediaImage);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                delete = itemView.findViewById(R.id.delete); // Inicialización del botón de eliminación
                edit = itemView.findViewById(R.id.edit);
            }

        }
    }
}

