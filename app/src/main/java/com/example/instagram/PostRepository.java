package com.example.instagram;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

public class PostRepository {
    private FirebaseFirestore db;
    private Context context;

    // Constructor que recibe el contexto
    public PostRepository(Context context) {
        // Inicializa Firestore
        db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    // Método para eliminar un post
    public void deletePost(String postId, String userId) {
        // Construye la referencia al documento del post que se va a eliminar
        DocumentReference postRef = db.collection("posts").document(postId);

        // Elimina el post solo si el usuario actual es el autor del post
        postRef.delete()
                .addOnSuccessListener(aVoid -> {
                    showToast("Post eliminado con éxito");
                })
                .addOnFailureListener(e -> {
                    showToast("Error al eliminar el post");
                });
    }
    // Método para mostrar mensajes Toast
    private void showToast(String message) {
        // Puedes personalizar la duración y la posición del mensaje Toast según tus necesidades
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
