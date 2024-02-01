package com.example.instagram;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import com.example.instagram.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link editPost#newInstance} factory method to
 * create an instance of this fragment.
 */
public class editPost extends Fragment {

    private static final String ARG_POST_CONTENT = "post_content";
    private EditText editTextPostContent;
    private Button btnSaveChanges;
    private String postContent;
    static String postId;
    private OnPostEditListener onPostEditListener;
    private FirebaseFirestore db;
    private DocumentReference postRef;
    public editPost() {
        // Required empty public constructor
    }

    public static editPost newInstance(String postContent) {
        editPost fragment = new editPost();
        Bundle args = new Bundle();
        args.putString(ARG_POST_CONTENT, postContent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener la instancia de FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Obtener la referencia al documento del post
        postRef = db.collection("posts").document(postId);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el diseño para este fragmento
        View view = inflater.inflate(R.layout.fragment_edit_post, container, false);

        // Inicializar vistas
        editTextPostContent = view.findViewById(R.id.postContentEditText);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        // Configurar el clic del botón para guardar los cambios
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el contenido editado
                String editedContent = editTextPostContent.getText().toString();

                // Actualizar el contenido del post en Firestore
                postRef.update("content", editedContent)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Éxito al actualizar el contenido
                                Toast.makeText(getActivity(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
                                // Cerrar el fragmento
                                getParentFragmentManager().popBackStack();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error al actualizar el contenido
                                Toast.makeText(getActivity(), "Failed to save changes", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        return view;
    }

    // Método para establecer el listener para manejar eventos de edición de publicaciones

    public interface OnPostEditListener {
        void onPostEdited(String postId,String editedContent);
    }

    public void setOnPostEditListener(OnPostEditListener listener) {
        this.onPostEditListener = listener;
    }
    public static void obtenerKey(String key){
        postId= key;

    }
}