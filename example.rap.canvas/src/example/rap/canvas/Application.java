/*******************************************************************************
 * Copyright (c) 2012 EclipseSource.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elias Volanakis - initial API and implementation
 *******************************************************************************/
package example.rap.canvas;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Demonstrates how to use the GC class to draw over an image 'on the fly'.
 */
public class Application implements IApplication {

	private Image image;
	private List<Point> points = new ArrayList<Point>();

	public Object start(IApplicationContext context) throws Exception {
		Display display = Display.getDefault();
		Shell shell = new Shell(display, SWT.TITLE);
		shell.setText("example.rap.canvas");
		shell.setMaximized(true);
		shell.setLayout(new FillLayout());

		// 1. Create an image resource from a file (icons/turtle.png)
		image = AbstractUIPlugin.imageDescriptorFromPlugin(
				"example.rap.canvas", "icons/turtle.png").createImage();
		createCanvas(shell);

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		image.dispose();
		display.dispose();

		return null;
	}

	public void stop() {
		// unused
	}

	// helping methods
	// ////////////////

	/**
	 * 2. Create a Canvas widget. This is a blank area that we can paint on,
	 * with the help of a PaintListener object.
	 */
	private Canvas createCanvas(Composite parent) {
		Canvas result = new Canvas(parent, SWT.NONE);
		result.setBackground(result.getDisplay()
				.getSystemColor(SWT.COLOR_WHITE));
		result.addMouseListener(new CanvasMouseListener());
		result.addPaintListener(new CanvasPaintListener());
		return result;
	}

	// helping classes
	// ////////////////

	/**
	 * 3. We use a MouseListener to react to the user's clicks. A left click
	 * adds a Point. Any other click removes a point. The click triggers a
	 * redraw operation.
	 */
	private final class CanvasMouseListener extends MouseAdapter {
		private static final long serialVersionUID = 1L;

		@Override
		public void mouseDown(MouseEvent e) {
			if (e.button == 1) {
				Point p = new Point(e.x, e.y);
				points.add(p);
				System.out.println("Added point: " + p);
				((Canvas) e.widget).redraw();
			} else {
				if (!points.isEmpty()) {
					Point p = points.remove(points.size() - 1);
					System.out.println("Removed point: " + p);
					((Canvas) e.widget).redraw();
				}
			}
		}
	}

	/**
	 * 4. We use a PointListener to draw on the Canvas. The listener has access
	 * to a GC object. This is our 'brush' for drawing on the canvas. We draw
	 * the image first and then a sequence of lines, using the stored points
	 * (step 3).
	 */
	private final class CanvasPaintListener implements PaintListener {
		private static final long serialVersionUID = 1L;

		public void paintControl(PaintEvent event) {
			GC gc = event.gc;
			gc.drawImage(image, 0, 0);
			int width = 4;
			gc.setLineWidth(width);
			gc.setForeground(event.widget.getDisplay().getSystemColor(
					SWT.COLOR_GREEN));
			if (points.size() == 1) { // only a single point: draw a circle
				Point start = points.get(0);
				int offset = width / 2;
				gc.drawOval(start.x - offset, start.y - offset, width, width);
			} else { // multiple points: draw lines
				Point start = null;
				for (Point p : points) {
					if (start != null) {
						gc.drawLine(start.x, start.y, p.x, p.y);
					}
					start = p;
				}
			}
		}
	}

}
