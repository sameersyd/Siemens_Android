package com.sameer.siemens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DrawInterface;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ImagePickerCallback {

    Document document;
    EditText agreeNumEdit,custNameEdit,addressEdit;
    Button generatePdfBtn;
    TextView selectTxt;
    private ImagePicker imagePicker;
    List<ChosenImage> imgList = null;
    public static final String DEST = Environment.getExternalStorageDirectory().getPath() + "/siemensPDF/"+"Siemens.pdf";

    View alpView;
    ProgressBar pg;
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        agreeNumEdit = (EditText)findViewById(R.id.main_agreeNum);
        custNameEdit = (EditText)findViewById(R.id.main_custName);
        addressEdit = (EditText)findViewById(R.id.main_address);
        selectTxt = (TextView) findViewById(R.id.main_selectImgTxt);
        generatePdfBtn = (Button) findViewById(R.id.main_generatePdfBtn);
        alpView = (View)findViewById(R.id.main_view);
        pg = (ProgressBar)findViewById(R.id.main_progress);

        selectTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePick();
            }
        });

        generatePdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted())
                    generatePdfMethod();
            }
        });

    }

    public void generatePdfMethod(){

        if (agreeNumEdit.getText().toString().isEmpty() || agreeNumEdit.getText().toString() == ""){
            Toast.makeText(this, "Enter Agreement Number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (custNameEdit.getText().toString().isEmpty() || custNameEdit.getText().toString() == ""){
            Toast.makeText(this, "Enter Customer Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (addressEdit.getText().toString().isEmpty() || addressEdit.getText().toString() == ""){
            Toast.makeText(this, "Enter Address", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            showHUD();

            document = new Document(PageSize.A4);

            String root = Environment.getExternalStorageDirectory().toString();

            File myDir = new File(root + "/siemensPDF");
            myDir.mkdir();

            PdfWriter.getInstance(document,new FileOutputStream(DEST));
            document.open();

            addTitlePage(document);

            document.close();

            Toast.makeText(this, "PDF Generated", Toast.LENGTH_SHORT).show();
            agreeNumEdit.setText("");
            custNameEdit.setText("");
            addressEdit.setText("");
            imgList = null;

            hideHUD();

        } catch (DocumentException e) {
            hideHUD();
            Toast.makeText(this, e+"", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            hideHUD();
            Toast.makeText(this, e+"", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            hideHUD();
            Toast.makeText(this, e+"", Toast.LENGTH_SHORT).show();
        }

    }

    private void addTitlePage(Document document) throws DocumentException, IOException {


        Font titleFont = new Font(Font.FontFamily.HELVETICA,24,Font.BOLD,BaseColor.DARK_GRAY);
        Font subHeadFont = new Font(Font.FontFamily.HELVETICA,18,Font.BOLD,BaseColor.BLACK);
        Chunk c = new Chunk("General Information", subHeadFont);
        Chunk chunk2 = new Chunk("Asset Condition", subHeadFont);
        Chunk chunk3 = new Chunk("Information About Onsite Visit", subHeadFont);

        c.setBackground(BaseColor.LIGHT_GRAY);
        chunk2.setBackground(BaseColor.LIGHT_GRAY);
        chunk3.setBackground(BaseColor.LIGHT_GRAY);


        Paragraph prHead = new Paragraph();
        prHead.setSpacingAfter(30f);
        prHead.setFont(titleFont);
        prHead.setAlignment(Element.ALIGN_LEFT);
        prHead.add("SIEMENS"+ "\n");


        // General Information Table

        Paragraph genInfo = new Paragraph(c);
        genInfo.setFont(subHeadFont);
        genInfo.setAlignment(Element.ALIGN_CENTER);
        genInfo.setSpacingAfter(30f);


        PdfPTable table = new PdfPTable(2);

        PdfPCell c1 = new PdfPCell(new Phrase("Agreement Number"));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("A88677"));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c1);

        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell("Label Number: ");
        table.addCell("Not verified");
        table.addCell("Customer Name: ");
        table.addCell( custNameEdit.getText().toString());
        table.addCell("Address of Asset Location: ");
        table.addCell(addressEdit.getText().toString());
        table.addCell("Name Plate");
        table.addCell("Available");
        table.addCell("Serial Number (On Asset)");
        table.addCell("Available");
        table.addCell("Serial number(in Invoice): (If Available)");
        table.addCell(" - ");
        table.addCell("Type of Foundation for Asset (Wherever applicable)");
        table.addCell("Concrete Floor");
        table.addCell("Description of the premises :");
        table.addCell("Manufacturing Facility");
        table.addCell("Asset influencing circumstances (i.e Chemical Zone, Corrosive atomosphere, Highly Polluted Location etc..) : ");
        table.addCell("Yes");



        //Asset Condition Table

        Paragraph asset = new Paragraph(chunk2);
        asset.setFont(subHeadFont);
        asset.setAlignment(Element.ALIGN_CENTER);
        asset.setSpacingAfter(30f);
        asset.setSpacingBefore(30f);


        PdfPTable table2 = new PdfPTable(2);

        PdfPCell c2 = new PdfPCell(new Phrase("Other impression of physical condition of Asset : "));
        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(c2);

        c2 = new PdfPCell(new Phrase("Good"));
        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(c2);

        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell("Label Number: ");
        table2.addCell("Not verified");
        table2.addCell("Was the asset in operation at the time of inspection");
        table2.addCell("Yes");
        table2.addCell("Age of the Asset at the time of purchase");
        table2.addCell("New");
        table2.addCell("Any Breakdown in the machine");
        table2.addCell("Not Know");
        table2.addCell("Photos: ");
        table2.addCell("Yes");


        //  Information About ONsite Visit Table

        Paragraph onsite = new Paragraph(chunk3);
        onsite.setFont(subHeadFont);
        onsite.setAlignment(Element.ALIGN_CENTER);
        onsite.setSpacingAfter(30f);


        PdfPTable table3 = new PdfPTable(2);

        PdfPCell c3 = new PdfPCell(new Phrase("Date of Onsite Visit : "));
        c3.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(c3);

        c3 = new PdfPCell(new Phrase("13/06/2018"));
        c3.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(c3);

        c3.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell("Customer Personnel met at site : ");
        table3.addCell("Mr. Anand - DGM - Finance");
        table3.addCell("Report created at");
        table3.addCell("Bangalore");
        table3.addCell("Inspection visit done by: ");
        table3.addCell("Vijendra K");



        document.add(prHead);
        document.add(genInfo);
        document.add(table);
        document.add(asset);
        document.add(table2);
        document.add(onsite);
        document.add(table3);


        if (imgList != null && !imgList.isEmpty()) {
            for (int i=0;i<imgList.size();i++){
                Uri originalFileUri = Uri.parse(imgList.get(i).getOriginalPath());
                try {
                    uploadImage(originalFileUri.getPath());
                } catch (Exception ex) {
                    Toast.makeText(this, ex+"", Toast.LENGTH_SHORT).show();
                }
            }
        }

        document.newPage();
    }

    public void openImagePick() {
        imagePicker = new ImagePicker(this);
        imagePicker.shouldGenerateMetadata(true);
        imagePicker.shouldGenerateThumbnails(true);
        imagePicker.setImagePickerCallback(this);
        imagePicker.allowMultiple();
        imagePicker.pickImage();
    }


    @Override
    public void onImagesChosen(List<ChosenImage> list) {
        imgList = list;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case Picker.PICK_IMAGE_DEVICE:
                    if (imagePicker == null) {
                        imagePicker = new ImagePicker(this);
                        imagePicker.setImagePickerCallback(this);
                    }
                    imagePicker.submit(data);
                    break;
            }
        }
    }

    public void uploadImage(String filePath){
        try{
            InputStream ims = new FileInputStream(filePath);
            Bitmap bimp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream strm = new ByteArrayOutputStream();
            bimp.compress(Bitmap.CompressFormat.PNG, 100, strm);
            Image image1 = Image.getInstance(strm.toByteArray());
            image1.scaleToFit(400, 400);
            image1.setSpacingAfter(30f);

            document.add(image1);

        }catch (Exception e){
            Toast.makeText(this, e+"", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onError(String s) {
        Toast.makeText(this, s+"", Toast.LENGTH_SHORT).show();
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            generatePdfMethod();
        }
    }

    public void showHUD(){
        pg.setVisibility(View.VISIBLE);
        alpView.setVisibility(View.VISIBLE);
        generatePdfBtn.setEnabled(false);
        selectTxt.setEnabled(false);
    }

    public void hideHUD(){
        pg.setVisibility(View.GONE);
        alpView.setVisibility(View.GONE);
        generatePdfBtn.setEnabled(true);
        selectTxt.setEnabled(true);
    }

}
