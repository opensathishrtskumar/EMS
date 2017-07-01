package com.ems.UI.custom.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.GroupDTO;

public class DnDJTree extends JTree implements DragSourceListener, DropTargetListener, DragGestureListener {
	private static final Logger logger = LoggerFactory.getLogger(DnDJTree.class);

	private static final long serialVersionUID = 1L;
	static DataFlavor localObjectFlavor;
	static {
		try {
			localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	static DataFlavor[] supportedFlavors = { localObjectFlavor };
	DragSource dragSource;
	DropTarget dropTarget;
	TreeNode dropTargetNode = null;
	TreeNode draggedNode = null;
	private boolean target = false;
	
	public DnDJTree() {
		super();
		setCellRenderer(new DnDTreeCellRenderer());
		setModel(new DefaultTreeModel(new DefaultMutableTreeNode("default")));
		dragSource = new DragSource();
		DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
		dropTarget = new DropTarget(this, this);
	}
	
	public boolean isTarget() {
		return target;
	}

	public void setTarget(boolean target) {
		this.target = target;
	}

	// DragGestureListener
	public void dragGestureRecognized(DragGestureEvent dge) {
		logger.debug("dragGestureRecognized");
		// find object at this x,y
		Point clickPoint = dge.getDragOrigin();
		TreePath path = getPathForLocation(clickPoint.x, clickPoint.y);
		if (path == null) {
			logger.debug("not on a node");
			return;
		}
		draggedNode = (TreeNode) path.getLastPathComponent();
		Transferable trans = new RJLTransferable(draggedNode);
		dragSource.startDrag(dge, Cursor.getDefaultCursor(), trans, this);
	}

	// DragSourceListener events
	public void dragDropEnd(DragSourceDropEvent dsde) {
		logger.debug("dragDropEnd()");
		dropTargetNode = null;
		draggedNode = null;
		repaint();
	}

	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	// DropTargetListener events
	public void dragEnter(DropTargetDragEvent dtde) {
		logger.debug("dragEnter");
		dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		logger.debug("accepted dragEnter");
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
		// figure out which cell it's over, no drag to self
		Point dragPoint = dtde.getLocation();
		TreePath path = getPathForLocation(dragPoint.x, dragPoint.y);
		if (path == null)
			dropTargetNode = null;
		else
			dropTargetNode = (TreeNode) path.getLastPathComponent();
		repaint();
	}

	public void drop(DropTargetDropEvent dtde) {
		logger.debug("drop()!");
		Point dropPoint = dtde.getLocation();
		// int index = locationToIndex (dropPoint);
		TreePath path = getPathForLocation(dropPoint.x, dropPoint.y);
		logger.debug("drop path is " + path);

		//avoid target to source DnD
		if(!target){
			dtde.rejectDrop();
			return;
		}
		
		DefaultMutableTreeNode target = (DefaultMutableTreeNode)dropTargetNode;
		logger.debug("Target " + target);

		if (dropTargetNode.isLeaf() && target.getUserObject() instanceof DeviceDetailsDTO) {
			logger.debug("Target is leaf can't drop");
			dtde.rejectDrop();
			return;
		}

		boolean dropped = false;
		try {
			dtde.acceptDrop(DnDConstants.ACTION_MOVE);
			logger.debug("accepted");
			Object droppedObject = dtde.getTransferable().getTransferData(localObjectFlavor);

			if (droppedObject == draggedNode) {
				logger.debug("dropped onto self");
				dtde.rejectDrop();
				return;
			}
			
			if (droppedObject instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode targetGroup = (DefaultMutableTreeNode)droppedObject;
				logger.info(targetGroup.getUserObject().getClass().getCanonicalName());
				DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				if(targetGroup.getUserObject() instanceof DeviceDetailsDTO && dropNode.getUserObject() instanceof GroupDTO){
					DefaultTreeModel treeModel = (DefaultTreeModel)getModel();
					treeModel.insertNodeInto(targetGroup, dropNode, 0);
					logger.info("New child added");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getLocalizedMessage());
		}
		dtde.dropComplete(true);
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	class RJLTransferable implements Transferable {
		Object object;

		public RJLTransferable(Object o) {
			object = o;
		}

		public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(df))
				return object;
			else{
				logger.error("Data flavour is unsupported");
				throw new UnsupportedFlavorException(df);
			}
		}

		public boolean isDataFlavorSupported(DataFlavor df) {
			return (df.equals(localObjectFlavor));
		}

		public DataFlavor[] getTransferDataFlavors() {
			return supportedFlavors;
		}
	}

	class DnDTreeCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;
		boolean isTargetNode;
		boolean isTargetNodeLeaf;
		boolean isLastItem;
		Insets normalInsets, lastItemInsets;
		int BOTTOM_PAD = 30;

		public DnDTreeCellRenderer() {
			super();
			normalInsets = super.getInsets();
			lastItemInsets = new Insets(normalInsets.top, normalInsets.left, normalInsets.bottom + BOTTOM_PAD,
					normalInsets.right);
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded,
				boolean isLeaf, int row, boolean hasFocus) {
			isTargetNode = (value == dropTargetNode);
			isTargetNodeLeaf = (isTargetNode && ((TreeNode) value).isLeaf());
			// isLastItem = (index == list.getModel().getSize()-1);
			boolean showSelected = isSelected & (dropTargetNode == null);
			return super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (isTargetNode) {
				g.setColor(Color.black);
				if (isTargetNodeLeaf) {
					g.drawLine(0, 0, getSize().width, 0);
				} else {
					g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
				}
			}
		}
	}
}