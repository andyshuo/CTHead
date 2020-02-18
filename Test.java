import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;

// OK this is not best practice - maybe you'd like to create
// a volume data class?
// I won't give extra marks for that though.

public class Test extends Application
{
	short cthead[][][]; // store the 3D volume data set
	short min, max; // min/max value in the 3D volume data set

	@Override
	public void start(Stage stage) throws FileNotFoundException, IOException
	{
		stage.setTitle("CThead Viewer");
		

		ReadData();

		int width = 256;
		int height = 256;
		WritableImage medical_imageZ = new WritableImage(width, height);
		WritableImage medical_imageY = new WritableImage(width, height);
		WritableImage medical_imageX = new WritableImage(width, height);
		WritableImage MIPZ = new WritableImage(width, height);
		WritableImage MIPY = new WritableImage(width, height);
		WritableImage MIPX = new WritableImage(width, height);
		ImageView imageViewZ = new ImageView(medical_imageZ);
		ImageView imageViewY = new ImageView(medical_imageY);
		ImageView imageViewX = new ImageView(medical_imageX);
		ImageView viewZ = new ImageView(MIPZ);
		ImageView viewY = new ImageView(MIPY);
		ImageView viewX = new ImageView(MIPX);

		Button mip_button = new Button("MIP"); // an example button to switch to MIP mode
		// sliders to step through the slices (z and y directions) (remember 113 slices
		// in z direction 0-112)
		Button resizeMIP = new Button("Resize Nearest Neighbour");
		Button resizeBilinear = new Button("Resize Bilinear");
		Slider zslider = new Slider(0, 112, 0);
		Slider yslider = new Slider(0, 255, 0);
		Slider xslider = new Slider(0, 255, 0);
		
		resizeBilinear.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event)
			{
				viewY.setImage(resizeBilinear(MIPY));
				viewX.setImage(resizeBilinear(MIPX));
			}
		});
		
		resizeMIP.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event)
			{
				viewY.setImage(resizing(MIPY));
				viewX.setImage(resizing(MIPX));
			}
		});

		mip_button.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				MIPALL(MIPZ, MIPY, MIPX);
			}
		});

		zslider.valueProperty().addListener(new ChangeListener<Number>()
		{
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				MIPZ(medical_imageZ, newValue.intValue());
				resizing(medical_imageY);
				System.out.println(newValue.intValue());
			}
		});

		yslider.valueProperty().addListener(new ChangeListener<Number>()
		{
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				MIPY(medical_imageY, newValue.intValue());
				System.out.println(newValue.intValue());
			}
		});

		xslider.valueProperty().addListener(new ChangeListener<Number>()
		{
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				MIPX(medical_imageX, newValue.intValue());
				System.out.println(newValue.intValue());
			}
		});

		FlowPane root = new FlowPane();
		root.setVgap(8);
		root.setHgap(4);
//https://examples.javacodegeeks.com/desktop-java/javafx/scene/image-scene/javafx-image-example/

		root.getChildren().addAll(imageViewZ, imageViewY, imageViewX, mip_button,resizeMIP,resizeBilinear, zslider, yslider, xslider, viewZ,
				viewY, viewX);


		
		Scene scene = new Scene(root, 1000, 900);
		stage.setScene(scene);
		stage.show();
	}

	public void MIPALL(WritableImage imageZ, WritableImage imageY, WritableImage imageX)
	{
		PixelWriter image_writerZ = imageZ.getPixelWriter();
		PixelWriter image_writerY = imageY.getPixelWriter();
		PixelWriter image_writerX = imageX.getPixelWriter();

		short datum;


		for (int j = 0; j < imageZ.getHeight(); j++)
		{
			for (int i = 0; i < imageZ.getWidth(); i++)
			{
				float col = 0;
				for (int k = 0; k < 113; k++)
				{
					datum = cthead[k][j][i];

					float temp = (((float) datum - (float) min) / ((float) (max - min)));
					if (temp > col)
					{
						col = temp;
					}
					for (int c = 0; c < 3; c++)
					{
						image_writerZ.setColor(i, j, new Color(col, col, col, 1.0));
					}
				}
			}
		}
		
		
		for(int k=0;k<113;k++)
		{
			for(int i=0;i<256;i++)
			{
				float col =0;
				for(int j=0;j<256;j++)
				{
					datum = cthead[k][j][i];
					float temp = (((float) datum - (float) min) / ((float) (max - min)));
					if(temp>col)
					{
						col=temp;
					}
					for(int c=0;c<3;c++)
					{
						image_writerY.setColor(i,k,new Color(col,col,col,1.0));
					}
				}
			}
		}
		
		
		for(int k=0;k<113;k++)
		{
			for(int j=0;j<256;j++)
			{
				float col = 0;
				for(int i=0;i<256;i++)
				{
					datum = cthead[k][j][i];
					float temp = (((float) datum - (float) min) / ((float) (max - min)));
					if(temp > col)
					{
						col = temp;
					}
					for(int c=0;c<3;c++)
					{
						image_writerX.setColor(j,k,new Color(col,col,col,1.0));
					}
				}
			}
		}

	}

	// Function to read in the cthead data set
	public void ReadData() throws IOException
	{
		// File name is hardcoded here - much nicer to have a dialog to select it and
		// capture the size from the user
		File file = new File("CThead");
		// Read the data quickly via a buffer (in C++ you can just do a single fread - I
		// couldn't find if there is an equivalent in Java)
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		int i, j, k; // loop through the 3D data set

		min = Short.MAX_VALUE;
		max = Short.MIN_VALUE; // set to extreme values
		short read; // value read in
		int b1, b2; // data is wrong Endian (check wikipedia) for Java so we need to swap the bytes
					// around

		cthead = new short[113][256][256]; // allocate the memory - note this is fixed for this data set
		// loop through the data reading it in
		for (k = 0; k < 113; k++)
		{
			for (j = 0; j < 256; j++)
			{
				for (i = 0; i < 256; i++)
				{
					// because the Endianess is wrong, it needs to be read byte at a time and
					// swapped
					b1 = ((int) in.readByte()) & 0xff; // the 0xff is because Java does not have unsigned types
					b2 = ((int) in.readByte()) & 0xff; // the 0xff is because Java does not have unsigned types
					read = (short) ((b2 << 8) | b1); // and swizzle the bytes around
					if (read < min)
						min = read; // update the minimum
					if (read > max)
						max = read; // update the maximum
					cthead[k][j][i] = read; // put the short into memory (in C++ you can replace all this code with one
											// fread)
				}
			}
		}
		System.out.println(min + " " + max); // diagnostic - for CThead this should be -1117, 2248
		// (i.e. there are 3366 levels of grey (we are trying to display on 256 levels
		// of grey)
		// therefore histogram equalization would be a good thing
	}

	/*
	 * This function shows how to carry out an operation on an image. It obtains the
	 * dimensions of the image, and then loops through the image carrying out the
	 * copying of a slice of data into the image.
	 */
	public void MIPZ(WritableImage image, int k)
	{
		// Get image dimensions, and declare loop variables
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		// Shows how to loop through each pixel and colour
		// Try to always use j for loops in y, and i for loops in x
		// as this makes the code more readable
		for (j = 0; j < h; j++)
		{
			for (i = 0; i < w; i++)
			{
				// at this point (i,j) is a single pixel in the image
				// here you would need to do something to (i,j) if the image size
				// does not match the slice size (e.g. during an image resizing operation
				// If you don't do this, your j,i could be outside the array bounds
				// In the framework, the image is 256x256 and the data set slices are 256x256
				// so I don't do anything - this also leaves you something to do for the
				// assignment
				datum = cthead[k][j][i]; // get values from slice 76 (change this in your assignment)
				// calculate the colour by performing a mapping from [min,max] -> [0,255]
				col = (((float) datum - (float) min) / ((float) (max - min)));
				for (c = 0; c < 3; c++)
				{
					// and now we are looping through the bgr components of the pixel
					// set the colour component c of pixel (i,j)
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
					// data[c+3*i+3*j*w]=(byte) col;
				} // colour loop
			} // column loop
		} // row loop
	}

	public void MIPY(WritableImage image, int j)
	{
		// Get image dimensions, and declare loop variables
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		// Shows how to loop through each pixel and colour
		// Try to always use j for loops in y, and i for loops in x
		// as this makes the code more readable
		for (k = 0; k < 113; k++)
		{
			for (i = 0; i < w; i++)
			{
				// at this point (i,j) is a single pixel in the image
				// here you would need to do something to (i,j) if the image size
				// does not match the slice size (e.g. during an image resizing operation
				// If you don't do this, your j,i could be outside the array bounds
				// In the framework, the image is 256x256 and the data set slices are 256x256
				// so I don't do anything - this also leaves you something to do for the
				// assignment
				datum = cthead[k][j][i]; // get values from slice 76 (change this in your assignment)
				// calculate the colour by performing a mapping from [min,max] -> [0,255]
				col = (((float) datum - (float) min) / ((float) (max - min)));
				for (c = 0; c < 3; c++)
				{
					// and now we are looping through the bgr components of the pixel
					// set the colour component c of pixel (i,j)
					image_writer.setColor(i, k, Color.color(col, col, col, 1.0));
					// data[c+3*i+3*j*w]=(byte) col;
				} // colour loop
			} // column loop
		} // row loop
		
		

		PixelReader pr = image.getPixelReader();
		
		for(int y=0;y<256;y++)
		{
			for(int x=0;x<256;x++)
			{
				Color temp = pr.getColor(x, y);
				int a = y*(112/256);
				for(int v=0;v<3;v++)
				{
					image_writer.setColor(x,a,Color.color(temp.getRed(),temp.getGreen(),temp.getBlue(),1.0));
				}
			}
		}
	}

	public void MIPX(WritableImage image, int i)
	{
		// Get image dimensions, and declare loop variables
		int w = (int) image.getWidth(), h = (int) image.getHeight(), j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		// Shows how to loop through each pixel and colour
		// Try to always use j for loops in y, and i for loops in x
		// as this makes the code more readable
		for (j = 0; j < 256; j++)
		{
			for (k = 0; k < 113; k++)
			{
				// at this point (i,j) is a single pixel in the image
				// here you would need to do something to (i,j) if the image size
				// does not match the slice size (e.g. during an image resizing operation
				// If you don't do this, your j,i could be outside the array bounds
				// In the framework, the image is 256x256 and the data set slices are 256x256
				// so I don't do anything - this also leaves you something to do for the
				// assignment
				datum = cthead[k][j][i]; // get values from slice 76 (change this in your assignment)
				// calculate the colour by performing a mapping from [min,max] -> [0,255]
				col = (((float) datum - (float) min) / ((float) (max - min)));
				for (c = 0; c < 3; c++)
				{
					// and now we are looping through the bgr components of the pixel
					// set the colour component c of pixel (i,j)
					image_writer.setColor(j, k, Color.color(col, col, col, 1.0));
					// data[c+3*i+3*j*w]=(byte) col;
				} // colour loop
			} // column loop
		} // row loop
	}
	
	public WritableImage resizing(WritableImage image)
	{
		WritableImage result = new WritableImage(256,256);
		PixelWriter pw = result.getPixelWriter();
		PixelReader pr = image.getPixelReader();
		float ratio = ((float)112/(float)256);
		//System.out.println(ratio);
		
		for(int j=0;j<result.getHeight();j++)
		{
			for(int i=0;i<result.getWidth();i++)
			{
				for(int c=0;c<2;c++)
				{
					int y=Math.round(j*ratio);
					//System.out.println(y);
					Color temp = pr.getColor(i,y);
					pw.setColor(i,j,temp);
				}
			}
		}
		return result;
		
		
	}
	
	public WritableImage resizeBilinear(WritableImage image)
	{
		WritableImage result = new WritableImage(256,256);
		PixelWriter pw = result.getPixelWriter();
		PixelReader pr = image.getPixelReader();
		float ratioX=1;
		float ratioY = (float)112/(float)256;
		
		for(int j=0;j<256;j++)
		{
			for(int i=0;i<256;i++)
			{
				for(int c=0;c<2;c++)
				{
					
					int x1 = (int) Math.floor(i*1);
					int y1 = (int) Math.floor(j*ratioY);
					
					int x2 = x1+1;
					int y2 = y1;
					int x3 = x1;
					int y3 = y1+1;
					int x4 = x1+1;
					int y4 = y1+1;
					//System.out.println(y1);
					//System.out.println(y2);
					//System.out.println(y3);
					//System.out.println(y4);
					pw.setColor(i,j,getColor(pr,x1,x2,x3,x4,y1,y2,y3,y4, i,j, ratioX,ratioY));
					
				}
			}
		}
		return result;
	}
	
	public Color getColor(PixelReader pr, int x1,int x2,int x3,int x4, int y1,int y2, int y3,int y4, int x, int y, float rX, float rY)
	{
		if(x2==256)
		{
			float ratioY2 = (y-(y1/rY))/((y4/rY)-(y1/rY));
			Color t =colorDifference(pr.getColor(x1,y1),pr.getColor(x3,y3),ratioY2);
			return t;
		}
		float ratioX = (x-(x1/rX))/((x2/rX)-(x1/rX));
		//System.out.println(ratioX);
		Color tempX = colorDifference(pr.getColor(x1,y1),pr.getColor(x2,y2), ratioX);
		//System.out.println(1);
		float ratioY = (y-(y1/rY))/((y4/rY)-(y1/rY));
		//System.out.println(ratioY);
		Color tempX2 = colorDifference(pr.getColor(x3,y3),pr.getColor(x4,y4),ratioX);
		//System.out.println(2);
		Color tempY = colorDifference(tempX,tempX2,ratioY);
		//System.out.println(3);
		return tempY;
		//Color 
	}
	
	public Color colorDifference(Color a, Color b, float ratio)
	{
		//System.out.println(ratio);
		//System.out.println(a.getRed());
		//System.out.println(b.getRed());
		double red = a.getRed()+ratio*(b.getRed()-a.getRed());
		//System.out.println(red);
		double green = a.getGreen()+ratio*(b.getGreen()-a.getGreen());
		//System.out.println(green);
		double blue = a.getBlue()+ratio*(b.getBlue()-a.getBlue());
		//System.out.println(blue);
		return new Color(red,green,blue,1);
	}
	public Color colorAdd(Color a, Color b)
	{
		double red = a.getRed()+b.getRed();
		//System.out.println(red);
		double green = a.getGreen()+b.getGreen();
		//System.out.println(green);
		double blue = a.getBlue() + b.getBlue();
		//System.out.println(blue);
		return new Color(red,green,blue,1);
	}

	public static void main(String[] args)
	{
		launch();
	}

}