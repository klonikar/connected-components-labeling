package org.ml.ccl;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class CCL
{
    private int[][] _board;
    private BufferedImage _input;
    private int _width;
    private int _height;
    private int backgroundColor;

    public Map<Integer, BufferedImage> Process(BufferedImage input, int bgColor)
    {
    	backgroundColor = bgColor;
        _input = input;
        _width = input.getWidth();
        _height = input.getHeight();
        _board = new int[_width][];
        for(int i = 0;i < _width;i++)
        	_board[i] = new int[_height];

        Map<Integer, List<Pixel>> patterns = Find();
        Map<Integer, BufferedImage> images = new HashMap<Integer, BufferedImage>();

        for(Integer id : patterns.keySet())
        {
            BufferedImage bmp = CreateBitmap(patterns.get(id));
            images.put(id, bmp);
        }

        return images;
    }

    protected boolean CheckIsBackGround(Pixel currentPixel)
    {
    	// check if pixel color is backgroundColor (white).
        //return currentPixel.color.getAlpha() == 255 && currentPixel.color.getRed() == 255 && currentPixel.color.getGreen() == 255 && currentPixel.color.getBlue() == 255;
    	return currentPixel.color.getRGB() == backgroundColor;
    }

    private static int Min(List<Integer> neighboringLabels, Map<Integer, Label> allLabels) {
    	if(neighboringLabels.isEmpty())
    		return 0; // TODO: is 0 appropriate for empty list
    	
    	int ret = allLabels.get(neighboringLabels.get(0)).GetRoot().Name;
    	for(Integer n : neighboringLabels) {
    		int curVal = allLabels.get(n).GetRoot().Name;
    		ret = (ret < curVal ? ret : curVal);
    	}
    	return ret;
    }
    
    private static int Min(List<Pixel> pattern, boolean xOrY) {
    	if(pattern.isEmpty())
    		return 0; // TODO: is 0 appropriate for empty list
    	
    	int ret = (xOrY ? pattern.get(0).Position.x : pattern.get(0).Position.y);
    	for(Pixel p : pattern) {
    		int curVal = (xOrY ? p.Position.x : p.Position.y);
    		ret = (ret < curVal ? ret : curVal);
    	}
    	return ret;
    }

    private static int Max(List<Pixel> pattern, boolean xOrY) {
    	if(pattern.isEmpty())
    		return 0; // TODO: is 0 appropriate for empty list
    	
    	int ret = (xOrY ? pattern.get(0).Position.x : pattern.get(0).Position.y);
    	for(Pixel p : pattern) {
    		int curVal = (xOrY ? p.Position.x : p.Position.y);
    		ret = (ret > curVal ? ret : curVal);
    	}
    	return ret;
    }

    private Map<Integer, List<Pixel>> Find()
    {
        int labelCount = 1;
        Map<Integer, Label> allLabels = new HashMap<Integer, Label>();

        for (int i = 0; i < _height; i++)
        {
            for (int j = 0; j < _width; j++)
            {
                Pixel currentPixel = new Pixel(new Point(j, i), new Color(_input.getRGB(j, i)));

                if (CheckIsBackGround(currentPixel))
                {
                    continue;
                }

                List<Integer> neighboringLabels = GetNeighboringLabels(currentPixel);
                int currentLabel;

                if (neighboringLabels.isEmpty())
                {
                    currentLabel = labelCount;
                    allLabels.put(currentLabel, new Label(currentLabel));
                    labelCount++;
                }
                else
                {
                    currentLabel = Min(neighboringLabels, allLabels);
                    Label root = allLabels.get(currentLabel).GetRoot();

                    for (Integer neighbor : neighboringLabels)
                    {
                        if (root.Name != allLabels.get(neighbor).GetRoot().Name)
                        {
                            allLabels.get(neighbor).Join(allLabels.get(currentLabel));
                        }
                    }
                }

                _board[j][i] = currentLabel;
            }
        }


        Map<Integer, List<Pixel>> patterns = AggregatePatterns(allLabels);

        return patterns;
    }

    private List<Integer> GetNeighboringLabels(Pixel pix)
    {
        List<Integer> neighboringLabels = new ArrayList<Integer>();

        for (int i = pix.Position.y - 1; i <= pix.Position.y + 2 && i < _height - 1; i++)
        {
            for (int j = pix.Position.x - 1; j <= pix.Position.x + 2 && j < _width - 1; j++)
            {
                if (i > -1 && j > -1 && _board[j][i] != 0)
                {
                    neighboringLabels.add(_board[j][i]);
                }
            }
        }

        return neighboringLabels;
    }

    private Map<Integer, List<Pixel>> AggregatePatterns(Map<Integer, Label> allLabels)
    {
        Map<Integer, List<Pixel>> patterns = new HashMap<Integer, List<Pixel>>();

        for (int i = 0; i < _height; i++)
        {
            for (int j = 0; j < _width; j++)
            {
                int patternNumber = _board[j][i];

                if (patternNumber != 0)
                {
                    patternNumber = allLabels.get(patternNumber).GetRoot().Name;

                    if (!patterns.containsKey(patternNumber))
                    {
                        patterns.put(patternNumber, new ArrayList<Pixel>());
                    }

                    patterns.get(patternNumber).add(new Pixel(new Point(j, i), Color.BLACK));
                }
            }
        }

        return patterns;
    }

    private BufferedImage CreateBitmap(List<Pixel> pattern)
    {
        int minX = Min(pattern, true);
        int maxX = Max(pattern, true);

        int minY = Min(pattern, false);
        int maxY = Max(pattern, false);

        int width = maxX + 1 - minX;
        int height = maxY + 1 - minY;

        BufferedImage bmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (Pixel pix : pattern)
        {
            bmp.setRGB(pix.Position.x - minX, pix.Position.y - minY, pix.color.getRGB());//shift position by minX and minY
        }

        return bmp;
    }

    public static String getBaseFileName(String fileName) {
    	return fileName.substring(0, fileName.indexOf('.'));
    }
    
    public static String getFileNameExtension(String fileName) {
    	return fileName.substring(fileName.indexOf('.') + 1);
    }
    
    // Sample usage:
    // java org.ml.ccl.CCL images/one.png
    // java org.ml.ccl.CCL images/two.png -5000269
    public static void main(String[] args) {
    	if(args.length == 0) {
    		System.err.println("Usage: CCL imageFile [bgColor]");
    		return;
    	}
    	CCL ccl = new CCL();
    	try {
    		int bgColor = 0xFFFFFFFF; // white default background color
    		if(args.length == 2) {
    			bgColor = Integer.decode(args[1]);
    		}
    		BufferedImage img = ImageIO.read(new File(args[0]));
    		// TODO: Obtain background color.
    		// An attempt to obtain bg color automatically: Center of image.
    		//System.out.println("image bg color: " + img.getRGB(img.getWidth()/2, img.getHeight()/2));
    		Map<Integer, BufferedImage> components = ccl.Process(img, bgColor);
    		for(Integer c : components.keySet()) {
    			String format = getFileNameExtension(args[0]);
    			ImageIO.write(components.get(c), format, new File(getBaseFileName(args[0]) + "-component-" + c + "."  + format));
    		}
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
    }
}