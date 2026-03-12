package com.todoapp.ui.profile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.todoapp.R;
import com.todoapp.util.PreferencesManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * ProfileDialogFragment - Dialog for managing user profile and Google Sign-In.
 * 
 * Features:
 * - Display and edit profile image
 * - Google Sign-In / Sign-Out
 * - Local profile image storage
 */
@AndroidEntryPoint
public class ProfileDialogFragment extends DialogFragment {

    public interface OnDismissListener {
        void onDismiss();
    }

    @Inject
    PreferencesManager preferencesManager;

    private ShapeableImageView ivProfileImage;
    private FloatingActionButton fabChangePhoto;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private MaterialButton btnGoogleSignIn;
    private MaterialButton btnSignOut;
    private MaterialButton btnRemovePhoto;
    private MaterialButton btnManageAccount;
    private ImageButton btnClose;
    private LinearLayout layoutTheme;
    private LinearLayout layoutSignIn;
    private TextView tvCurrentTheme;

    private GoogleSignInClient googleSignInClient;
    
    private ActivityResultLauncher<Intent> signInLauncher;
    private OnDismissListener onDismissListener;
    
    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }
    private ActivityResultLauncher<String> imagePickerLauncher;

    public static ProfileDialogFragment newInstance() {
        return new ProfileDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        
        // Register activity result launchers
        signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                }
            }
        );
        
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    handleImageSelected(uri);
                }
            }
        );
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_profile, null);
        
        initViews(view);
        setupClickListeners();
        updateUI();
        
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        
        return dialog;
    }

    private void initViews(View view) {
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        fabChangePhoto = view.findViewById(R.id.fabChangePhoto);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
        btnSignOut = view.findViewById(R.id.btnSignOut);
        btnRemovePhoto = view.findViewById(R.id.btnRemovePhoto);
        btnManageAccount = view.findViewById(R.id.btnManageAccount);
        btnClose = view.findViewById(R.id.btnClose);
        layoutTheme = view.findViewById(R.id.layoutTheme);
        layoutSignIn = view.findViewById(R.id.layoutSignIn);
        tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme);
    }

    private void setupClickListeners() {
        fabChangePhoto.setOnClickListener(v -> showImagePickerOptions());
        
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        
        btnSignOut.setOnClickListener(v -> showSignOutConfirmation());
        
        btnRemovePhoto.setOnClickListener(v -> removeProfilePhoto());
        
        btnManageAccount.setOnClickListener(v -> openManageAccount());
        
        btnClose.setOnClickListener(v -> dismiss());
        
        layoutTheme.setOnClickListener(v -> showThemeDialog());
    }
    
    private void openManageAccount() {
        // Open Google Account management in browser
        Intent intent = new Intent(Intent.ACTION_VIEW, 
            Uri.parse("https://myaccount.google.com/"));
        startActivity(intent);
    }

    private void updateUI() {
        boolean isSignedIn = preferencesManager.isUserSignedIn();
        
        if (isSignedIn) {
            // Show signed-in state
            String userName = preferencesManager.getUserName();
            String userEmail = preferencesManager.getUserEmail();
            
            // Format greeting with "Hi, Name!"
            String greeting = !TextUtils.isEmpty(userName) ? 
                getString(R.string.hi_greeting, userName) : getString(R.string.user);
            tvUserName.setText(greeting);
            tvUserEmail.setText(!TextUtils.isEmpty(userEmail) ? userEmail : "");
            
            layoutSignIn.setVisibility(View.GONE);
            btnManageAccount.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.VISIBLE);
        } else {
            // Show signed-out state
            tvUserName.setText(R.string.guest_user);
            tvUserEmail.setText(R.string.sign_in_prompt);
            
            layoutSignIn.setVisibility(View.VISIBLE);
            btnManageAccount.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.GONE);
        }
        
        // Load profile image
        loadProfileImage();
        
        // Show remove photo button if there's a local image
        String localImagePath = preferencesManager.getLocalProfileImagePath();
        String googlePhotoUrl = preferencesManager.getUserPhotoUrl();
        boolean hasImage = !TextUtils.isEmpty(localImagePath) || !TextUtils.isEmpty(googlePhotoUrl);
        btnRemovePhoto.setVisibility(hasImage ? View.VISIBLE : View.GONE);
        
        // Update current theme text
        updateThemeText();
    }

    private void updateThemeText() {
        int currentTheme = preferencesManager.getThemeMode();
        String[] themeNames = {
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        };
        tvCurrentTheme.setText(themeNames[currentTheme]);
    }

    private void showThemeDialog() {
        String[] themes = {
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        };
        
        int currentTheme = preferencesManager.getThemeMode();
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.theme)
            .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
                preferencesManager.setThemeMode(which);
                applyTheme(which);
                updateThemeText();
                dialog.dismiss();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void applyTheme(int themeMode) {
        switch (themeMode) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    private void loadProfileImage() {
        String localImagePath = preferencesManager.getLocalProfileImagePath();
        String googlePhotoUrl = preferencesManager.getUserPhotoUrl();
        
        if (!TextUtils.isEmpty(localImagePath)) {
            // Load local image with cache busting
            File imageFile = new File(localImagePath);
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .signature(new ObjectKey(imageFile.lastModified()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .placeholder(R.drawable.profile_placeholder)
                    .into(ivProfileImage);
                return;
            }
        }
        
        if (!TextUtils.isEmpty(googlePhotoUrl)) {
            // Load Google profile photo
            Glide.with(this)
                .load(googlePhotoUrl)
                .circleCrop()
                .placeholder(R.drawable.profile_placeholder)
                .into(ivProfileImage);
            return;
        }
        
        // Show placeholder
        ivProfileImage.setImageResource(R.drawable.profile_placeholder);
    }

    private void showImagePickerOptions() {
        String[] options = {
            getString(R.string.choose_from_gallery),
            getString(R.string.cancel)
        };
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.change_profile_photo)
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    imagePickerLauncher.launch("image/*");
                }
            })
            .show();
    }

    private void handleImageSelected(Uri uri) {
        try {
            // Save image to app's internal storage
            Context context = requireContext();
            File outputDir = new File(context.getFilesDir(), "profile");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            File outputFile = new File(outputDir, "profile_image.jpg");
            
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                FileOutputStream outputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                
                // Save path to preferences
                preferencesManager.setLocalProfileImagePath(outputFile.getAbsolutePath());
                
                // Update UI
                loadProfileImage();
                btnRemovePhoto.setVisibility(View.VISIBLE);
                
                Toast.makeText(context, R.string.profile_photo_updated, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.error_updating_photo, Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            
            // Save user info
            preferencesManager.setUserSignedIn(true);
            preferencesManager.setUserName(account.getDisplayName());
            preferencesManager.setUserEmail(account.getEmail());
            
            if (account.getPhotoUrl() != null) {
                preferencesManager.setUserPhotoUrl(account.getPhotoUrl().toString());
            }
            
            // Update UI
            updateUI();
            
            Toast.makeText(requireContext(), R.string.sign_in_successful, Toast.LENGTH_SHORT).show();
            
        } catch (ApiException e) {
            // Show more detailed error message
            String errorMessage = "Sign-in failed: ";
            switch (e.getStatusCode()) {
                case 7:
                    errorMessage += "Network error. Check your internet connection.";
                    break;
                case 10:
                    errorMessage += "Developer error. SHA-1 fingerprint or package name mismatch.";
                    break;
                case 12500:
                    errorMessage += "Sign-in cancelled by user.";
                    break;
                case 12501:
                    errorMessage += "Sign-in cancelled.";
                    break;
                case 12502:
                    errorMessage += "Sign-in pending. Please wait.";
                    break;
                default:
                    errorMessage += "Error code: " + e.getStatusCode();
            }
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            android.util.Log.e("ProfileDialog", "Google Sign-In failed", e);
        }
    }

    private void showSignOutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sign_out)
            .setMessage(R.string.sign_out_confirmation)
            .setPositiveButton(R.string.sign_out, (dialog, which) -> signOut())
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void signOut() {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // Clear user info
            preferencesManager.clearUserProfile();
            
            // Update UI
            updateUI();
            
            Toast.makeText(requireContext(), R.string.signed_out_successfully, Toast.LENGTH_SHORT).show();
        });
    }

    private void removeProfilePhoto() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.remove_photo)
            .setMessage(R.string.remove_photo_confirmation)
            .setPositiveButton(R.string.remove, (dialog, which) -> {
                // Delete local image
                String localImagePath = preferencesManager.getLocalProfileImagePath();
                if (!TextUtils.isEmpty(localImagePath)) {
                    File imageFile = new File(localImagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                
                // Clear saved paths
                preferencesManager.setLocalProfileImagePath("");
                preferencesManager.setUserPhotoUrl("");
                
                // Update UI
                loadProfileImage();
                btnRemovePhoto.setVisibility(View.GONE);
                
                Toast.makeText(requireContext(), R.string.photo_removed, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }
}
