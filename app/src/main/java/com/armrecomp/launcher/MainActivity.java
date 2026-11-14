package com.armrecomp.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int PICK_NSP = 42;
    private LinearLayout library;
    private TextView status;
    private File gamesDir;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        gamesDir = new File(getFilesDir(), "game-library");
        if (!gamesDir.exists()) gamesDir.mkdirs();
        buildUi();
        refreshLibrary();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }
    private void buildUi() {
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(Color.rgb(13,16,23));
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(20), dp(28), dp(20), dp(24));
        scroll.addView(root); setContentView(scroll);
        TextView eyebrow = text("ANDROID  •  ARM64", 12, Color.rgb(117,190,255), true); root.addView(eyebrow);
        TextView title = text("ARM Recomp\nLauncher", 30, Color.WHITE, true); title.setPadding(0,dp(8),0,dp(8)); root.addView(title);
        TextView subtitle = text("Game package library and recompilation workspace", 14, Color.LTGRAY, false); root.addView(subtitle);

        Button importBtn = new Button(this); importBtn.setText("IMPORT NSP FILE"); importBtn.setTextColor(Color.WHITE); importBtn.setAllCaps(false); importBtn.setBackground(bg(Color.rgb(37,104,181),14));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(54)); bp.topMargin=dp(24); root.addView(importBtn,bp);
        importBtn.setOnClickListener(v -> chooseNsp());

        status = text("Select an NSP file you are authorized to use. The app stores the original file privately.", 13, Color.rgb(180,190,205), false);
        status.setPadding(dp(14),dp(12),dp(14),dp(12)); status.setBackground(bg(Color.rgb(27,34,46),12));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,-2); sp.topMargin=dp(12); root.addView(status,sp);

        TextView section = text("GAME LIBRARY", 13, Color.rgb(150,165,185), true); section.setPadding(0,dp(28),0,dp(12)); root.addView(section);
        library = new LinearLayout(this); library.setOrientation(LinearLayout.VERTICAL); root.addView(library);

        TextView note = text("IMPORTER STATUS\nNSP selection and private storage are implemented. This build does not decrypt protected packages, extract encrypted content, or contain a game recompilation engine. Those require a separately integrated, legally usable engine and compatible game data.", 12, Color.rgb(165,176,192), false);
        note.setPadding(dp(14),dp(14),dp(14),dp(14)); note.setBackground(bg(Color.rgb(24,29,39),12));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1,-2); np.topMargin=dp(24); root.addView(note,np);
        TextView version = text("Version 0.1.0 • Local import prototype", 11, Color.GRAY, false); version.setGravity(Gravity.CENTER); version.setPadding(0,dp(22),0,0); root.addView(version);
    }

    private void chooseNsp() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/octet-stream", "application/x-nintendo-switch-nsp"});
        startActivityForResult(i, PICK_NSP);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode != PICK_NSP || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        String name = displayName(uri);
        if (!name.toLowerCase(Locale.ROOT).endsWith(".nsp")) {
            status.setText("That file does not have an .nsp extension. Choose an NSP package."); return;
        }
        status.setText("Importing " + name + "…");
        try {
            String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
            File out = new File(gamesDir, System.currentTimeMillis() + "_" + safe);
            try (InputStream in = getContentResolver().openInputStream(uri); FileOutputStream fos = new FileOutputStream(out)) {
                if (in == null) throw new Exception("Cannot open selected file");
                byte[] buf = new byte[1024*1024]; int n; while ((n=in.read(buf))!=-1) fos.write(buf,0,n);
            }
            status.setText("Imported successfully: " + name + "\nStored in the app's private game library.");
            refreshLibrary();
        } catch (Exception e) { status.setText("Import failed: " + e.getMessage()); }
    }

    private String displayName(Uri uri) {
        String result = "game.nsp";
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) { int ix=c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if(ix>=0) result=c.getString(ix); }
        } catch(Exception ignored) {}
        return result;
    }
    private String size(long bytes) {
        if (bytes >= 1024L*1024*1024) return String.format(Locale.US,"%.2f GB",bytes/(1024d*1024*1024));
        if (bytes >= 1024L*1024) return String.format(Locale.US,"%.1f MB",bytes/(1024d*1024));
        return String.format(Locale.US,"%.1f KB",bytes/1024d);
    }
    private void refreshLibrary() {
        if (library == null) return; library.removeAllViews(); File[] files=gamesDir.listFiles();
        if (files == null || files.length == 0) { TextView empty=text("No packages imported yet.",14,Color.rgb(175,185,200),false); empty.setPadding(dp(14),dp(18),dp(14),dp(18)); empty.setBackground(bg(Color.rgb(24,29,39),12)); library.addView(empty); return; }
        for (File f: files) {
            LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(14),dp(13),dp(14),dp(13)); card.setBackground(bg(Color.rgb(24,31,43),12));
            TextView n=text(f.getName(),14,Color.WHITE,true); card.addView(n);
            TextView meta=text("NSP package • " + size(f.length()) + " • Imported locally",12,Color.rgb(165,180,200),false); meta.setPadding(0,dp(6),0,0); card.addView(meta);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(10); library.addView(card,p);
        }
    }
}
