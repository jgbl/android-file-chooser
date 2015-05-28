package br.com.thinkti.android.filechooser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class AdvFileChooser extends Activity {
	private File currentDir;
	private FileArrayAdapter adapter;
	private FileFilter fileFilter;
	private File fileSelected;
	private ArrayList<String> extensions;
	private boolean selectFolder = false;
	private boolean unicode;
	private boolean blnNew;
	private String DefaultDir;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view_newfile);
		
		
        Bundle extras = getIntent().getExtras();
		if (extras != null) {
			unicode = extras.getBoolean("blnUniCode", true);
			DefaultDir = extras.getString("DefaultDir");
			blnNew = extras.getBoolean("blnNew",false);
			if (extras.getStringArrayList("filterFileExtension") != null) {
				extensions = extras.getStringArrayList("filterFileExtension");				
				fileFilter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {						
						return ((pathname.isDirectory()) 
								|| ExtensionsMatch(pathname));
						}
				};
			}
			if (extras.getBoolean("selectFolder")) {				
				selectFolder = true;
				fileFilter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {						
						return (pathname.isDirectory());
					}
				};									
			}
			//fileSelected = new File(o.getPath());
			final EditText edFile = (EditText)findViewById(R.id.edFile);
			edFile.setText("");
			final Button btnSel = (Button)findViewById(R.id.btnSelect);
			btnSel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (edFile.getText()!=null && edFile.getText().length()>0)
					{
						fileSelected = new File(currentDir,edFile.getText().toString());
						Intent intent = new Intent();
						intent.putExtra("fileSelected", fileSelected.getAbsolutePath());
						intent.putExtra("blnUniCode", AdvFileChooser.this.unicode);
						AdvFileChooser.this.setResult(Activity.RESULT_OK, intent);
						AdvFileChooser.this.finish();
					}
				}
			});
			
			final Button btnCreateFolder = (Button)findViewById(R.id.btnCreateFolder);
			btnCreateFolder.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) 
				{
					CreateFolder(edFile.getText().toString());
				}
			});
			/*
			Intent intent = new Intent();
			intent.putExtra("fileSelected", fileSelected.getAbsolutePath());
			intent.putExtra("blnUniCode", this.unicode);
			setResult(Activity.RESULT_OK, intent);
			finish();
			*/
		}
		Uri uri = getIntent().getData();
		if (uri!=null)
		{
			try {
				fileSelected = new File(getRealPathFromURI(uri));
				if (fileSelected.exists())
				{
					currentDir = fileSelected.getParentFile();
					if (currentDir.list()!=null && currentDir.list().length>0)
					{
						EditText edFile = (EditText)findViewById(R.id.edFile);
						edFile.setText(fileSelected.getName());
					}
					else
					{
						uri = null;
						currentDir = null;
					}
				}
				else
				{
					fileSelected = null;
					uri = null;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fileSelected = null;
				uri = null;
			}
		}
		if (uri==null)
		{
			if (DefaultDir == null || DefaultDir.length()==0) DefaultDir=Environment.getExternalStorageDirectory().getPath();
			currentDir = new File(DefaultDir);
		}
		
		Toast.makeText(this, "Loading " + currentDir.getPath(), Toast.LENGTH_LONG).show();
		fill(currentDir);		
	}
	
	public String getRealPathFromURI(android.net.Uri contentURI) throws Exception 
	{
		android.database.Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = getContentResolver().query(contentURI, proj, null,
					null, null);
			if (cursor!=null)
			{
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				return cursor.getString(column_index);
			}
			else
			{
				return contentURI.getPath();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		try {
			getMenuInflater().inflate(R.menu.menu, menu);
			//findViewById(R.menu.main).setBackgroundColor(Color.BLACK);
			//.setBackgroundColor(Color.BLACK);
			//resize();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.mnuCreateFolder)
		{
			CreateFolder("");
		}
		return super.onOptionsItemSelected(item);
	
	}
	
	private void CreateFolder(String name)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.mnuCreateFolder));
		alert.setMessage(getString(R.string.mnuCreateFolder)
				);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setLines(1);
		input.setSingleLine();
		alert.setView(input);
		input.setText(name);
		alert.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						String value = input.getText().toString();
						value = value.replace("\n", "");
						try {
							File F = new File(currentDir, value);
							if (!F.exists())
							{
								F.mkdirs();
								fill(currentDir);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

		alert.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) 
					{
						
					}
				});

		alert.show();

	}
	
	private boolean ExtensionsMatch(File pathname)
	{
		String ext;
		if(pathname.getName().contains("."))
		{
			ext = pathname.getName().substring(pathname.getName().lastIndexOf("."));
		}
		else
		{
			return false;
		}
		
		if (extensions.contains(ext)) return true;
		
		for(String itext: extensions)
		{
			itext = itext.replace(".", "\\.");
			itext = itext.toLowerCase(Locale.getDefault());
			ext = ext.toLowerCase(Locale.getDefault());
			if (ext.matches(itext.replace("?", ".{1}").replace("*", ".*")))
					{
						return true;
					}
		}
		
		return false;
		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if ((!currentDir.getName().equals("sdcard")) && (currentDir.getParentFile() != null)) {
        		AlertDialog.Builder A = new AlertDialog.Builder(this);
				A.setPositiveButton(getString(R.string.yes),
						new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent();
								intent.putExtra("blnUniCode", AdvFileChooser.this.unicode);
								intent.putExtra("blnNew", blnNew);
								setResult(Activity.RESULT_CANCELED, intent);
								AdvFileChooser.this.finish();
							}
						});
				A.setNegativeButton(getString(R.string.no),
						new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) 
							{
								currentDir = currentDir.getParentFile();
					        	fill(currentDir);
							}
						});
				A.setMessage(getString(R.string.quit));
				A.setTitle("Question");
				A.show();

        	} else {
        		finish();
        	}
            return false;
        }
        return super.onKeyDown(keyCode, event);
	}

	private void fill(File f) {
		File[] dirs = null;
		if (fileFilter != null)
			dirs = f.listFiles(fileFilter);
		else 
			dirs = f.listFiles();
			
		this.setTitle(getString(R.string.currentDir) + ": " + f.getName());
		List<Option> dir = new ArrayList<Option>();
		List<Option> fls = new ArrayList<Option>();
		try {
			for (File ff : dirs) {
				if (ff.isDirectory() && !ff.isHidden())
					dir.add(new Option(ff.getName(), getString(R.string.folder), ff
							.getAbsolutePath(), true, false, false));
				else {
					if (!ff.isHidden())
						fls.add(new Option(ff.getName(), getString(R.string.fileSize) + ": "
								+ ff.length(), ff.getAbsolutePath(), false, false, false));
				}
			}
		} catch (Exception e) {

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (!f.getName().equalsIgnoreCase("sdcard")) {
			if (f.getParentFile() != null) dir.add(0, new Option("..", getString(R.string.parentDirectory), f.getParent(), false, true, true));
		}
		
		ListView listView = (ListView) findViewById(R.id.lvFiles);
		
		adapter = new FileArrayAdapter(listView.getContext(), R.layout.file_view,
				dir);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				Option o = adapter.getItem(position);
				if (!o.isBack())
					doSelect(o);
				else {
					currentDir = new File(o.getPath());
					fill(currentDir);
				}	
			}
			
		});
	}
	
	private void doSelect(final Option o) {
		if (o.isFolder() || o.isParent()) {
			if (!selectFolder) {
				currentDir = new File(o.getPath());
				fill(currentDir);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.optionSelection))
				       .setCancelable(false)
				       .setPositiveButton(getString(R.string.openFolder), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   currentDir = new File(o.getPath());
				        	   fill(currentDir);
				           }
				       })
				       .setNegativeButton(getString(R.string.selectThis), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   	fileSelected = new File(o.getPath());
								Intent intent = new Intent();
								intent.putExtra("fileSelected", fileSelected.getAbsolutePath());
								intent.putExtra("blnUniCode", AdvFileChooser.this.unicode);
								intent.putExtra("blnNew", blnNew);
								setResult(Activity.RESULT_OK, intent);
								finish();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}
		} else {
			//onFileClick(o);
			fileSelected = new File(o.getPath());
			EditText edFile = (EditText)findViewById(R.id.edFile);
			edFile.setText(fileSelected.getName());
			/*
			Intent intent = new Intent();
			intent.putExtra("fileSelected", fileSelected.getAbsolutePath());
			intent.putExtra("blnUniCode", this.unicode);
			setResult(Activity.RESULT_OK, intent);
			finish();
			*/
		}
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}
}
