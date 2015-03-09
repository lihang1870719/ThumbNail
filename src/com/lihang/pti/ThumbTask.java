package com.lihang.pti;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.hashids.Hashids;

public class ThumbTask {

	private final Config config;
	private final Hashids hashidMaker;

	public ThumbTask(Config config) {
		this.config = config;
		hashidMaker = new Hashids(config.get("hashids.alphabet"),
				config.getInt("hashids.length"));
	}

	public void run() throws Exception {

		DBConn conn = DBConn.make(config);//静态工厂方法替代构造器
		List<Integer> docIds = conn.getDocIds();
		println("docs to process: " + docIds);
		for (Iterator<Integer> itr = docIds.iterator(); itr.hasNext();) {
			int id = itr.next();
			println("processing " + id);
			incIndent();
			try {
				gen(id, conn);
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (Exception e) {
				println("error occurred: " + e.getMessage());
				println("use default thumbnail");
				conn.updateThumbnail(id,
						"http://static.docq.cn/thumbnails/default-pdf.png");
			}
			decIndent();
		}
		conn.close();
		println(docIds.size() + " docs processed");

	}

	private void gen(int id, DBConn conn) throws Exception {

		String hashid = hashidMaker.encode(id);
		println("hashid: " + hashid);

		String hashidPath = explodeIntoShortDirs(hashid);
		println("hashid path: " + hashidPath);

		String originalFilePath = config.get("dirs.docs") + "/" + hashidPath
				+ "/original.pdf";

		// 画出缩略图并存储
		File originalFile = new File(originalFilePath);
		if (!originalFile.exists()) {
			throw new RuntimeException(originalFile + " does not exist");
		}

		BufferedImage pageImage = renderFirstPage(originalFile);

		int pageW = pageImage.getWidth();
		int pageH = pageImage.getHeight();

		// 缩略图的大小
		int destW = config.getInt("thumbnail.width");
		int destH = config.getInt("thumbnail.height");

		// 比例
		double sx = (double) destW / (double) pageW;
		double sy = (double) destH / (double) pageH;

		// 边距
		int padX = 0;
		int padY = 0;

		BufferedImage thumbnail = new BufferedImage(destW, destH,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = thumbnail.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, destW, destH);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (sx > sy) {
			padX = (int) ((destW - pageW * sy) / 2);

			thumbnail.getGraphics().drawImage(
					pageImage.getScaledInstance((int) (pageW * sy), destH,
							Image.SCALE_SMOOTH), padX, 0, null);
		} else {

			padY = (int) ((destH - pageH * sx) / 2);
			/*************somethingwrong********************/
			thumbnail.getGraphics().drawImage(
					pageImage.getScaledInstance((int) (pageW * sy), destH,
							Image.SCALE_SMOOTH), 0, padY, null);

		}
		File saveDir = new File(config.get("dirs.thumbnails") + "/"
				+ hashidPath + "/v1-999");
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}
		File saveFile = new File(saveDir, "v1.png");
		ImageIO.write(thumbnail, "png", saveFile);

		println("thumbnail file created: " + saveFile);

		String thumbnailUrl = "http://static.docq.cn/thumbnails/" + hashidPath
				+ "/v1-999/v1.png";
		conn.updateThumbnail(id, thumbnailUrl);
		println("thumbnail url updated: " + id + ", " + thumbnailUrl);

	}

	private BufferedImage renderFirstPage(File file) throws Exception {
		PDFParser p = new PDFParser(new FileInputStream(file));
		p.parse();
		PDDocument doc = p.getPDDocument();
		PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
		BufferedImage image = page.convertToImage();
		doc.close();
		return image;
	}

	/**
	 * 把字符串拆成指定位数的多级目录, 如把 NWKQW4 变成 NW/KQ/W4
	 */
	public static String explodeIntoShortDirs(String str) {
		String path = str.substring(0, 2);
		for (int i = 2; i < str.length(); i += 2) {
			int end = Math.min(str.length(), i + 2);
			path += '/' + str.substring(i, end);
		}
		return path;
	}

	private static int indent = 0;

	protected static void incIndent() {
		indent += 2;
	}

	protected static void decIndent() {
		indent -= 2;
	}

	protected static void println(Object value) {
		for (int i = 0; i < indent; i++) {
			System.out.print(" ");
		}
		System.out.println(value);
	}

}
