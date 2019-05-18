package jsontreeparse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.io.FilenameUtils;
import com.google.gson.*;
import JTreeManager.TaggedTreeNode;
import Tag.CsvParser;
import Tag.Parser;
import Tag.TagHistory;
import exceptionHandler.LogFileWritingHandler;
import properties.PropertiesHandler;

/**
 *
 * @author Groupe PRO B-9
 * Cette classe permet d'explorer des arborescence de fichier et de creer un fichier json
 * Elle permet aussi de parser se fichier json pour creer un tree de TreeNode object.
 */
public class JsonTreeParser {

	private String[] hierarchyTag;
	private Parser p = new Parser();
	Properties properties = PropertiesHandler.parseProperties();

	/**
	 * Cette methode parse les tag (le type de dossier de l'arborescence)
	 * Exemple un dossier tag� pourrai etre un dossier de date ou d'heure.
	 * Ceci nous permet de simplifier diff�rentes op�ration sur le filtrage des images.
	 */
	public void parseHierarchyTag() {
		hierarchyTag = ((String) properties.get("hierarchyTag")).split("/");
	}

	/**
	 * Cette methode permet de g�n�rer un fichier json en explorant l'arborescence des fichiers
	 * @param rootDirectory Le dossier racine ou l'exploration de l'arborescence s'executera
	 */
	public void createJson(File rootDirectory,int history) {
		try {

			int i = 0;
			parseHierarchyTag(); // on obtient les tag de la hierarchie
			
			
			Properties properties = PropertiesHandler.parseProperties();

			// creation du fichier json
			BufferedWriter file = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(properties.getProperty("JsonBankPath")).getAbsolutePath()),
					StandardCharsets.UTF_8));
			
			JsonArray tree;
			JsonObject temp = new JsonObject();
			try {

				tree = setJson(rootDirectory, i,history); // appelle a setJson sur le dossier racine

				temp.add("root", tree);

			} catch (ArrayIndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(null, "the hierachy of the directory doesnt correspond to the taghierachy field in the conf.properties");
				LogFileWritingHandler.handleException(e.getMessage(), e.getStackTrace());
			}

			file.write(temp.toString());
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 *
	 * @param file the file or the directory that will be explored
	 * @param i the index of the tag in the hierarchytag array
	 * @return an JsonArray that containe the JPG, SubDirectory and subFile in the directory explored.
	 * @throws ArrayIndexOutOfBoundsException
	 */
	private JsonArray setJson(File file, int i,int history) throws ArrayIndexOutOfBoundsException {

		if (file.isDirectory()) {

			JsonArray dirArray = new JsonArray();
			File[] childrenDir = file.listFiles();
			
			// Si le dossier des enfant, on trie les enfant pour que leur nom soit dans le bon ordre
			if (childrenDir != null) {
				Arrays.sort(childrenDir);

				// pour chaque enfant
				for (File child : childrenDir) {

					// si c'est un dossier
					if (child.isDirectory()) {

						// on applique la methode setJson r�cursivement
						JsonObject temp = new JsonObject();
						temp.addProperty("name", child.getName());
						temp.addProperty("tag", hierarchyTag[i]);
						temp.add("nextDir", setJson(child, i + 1,history));
						dirArray.add(temp);

					} else {

						// on regarde si l'extention du fichier et la bonne
						if (FilenameUtils.getExtension(child.getName()).equals(properties.getProperty("imageType"))) {

							if(history == 0) {
							
							// on enregistre les tag de l'image dans un fichier history.json
							try {
								ArrayList<ArrayList<String>> arraytemp = CsvParser.getTag(p.getTag(child.getAbsolutePath()));
								TagHistory.saveTag(arraytemp, child.getAbsolutePath());

							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							}

							//on ajoute l'image au fichier de l'arborescence
							JsonObject temp = new JsonObject();
							temp.addProperty("nameImage", child.getName());
							temp.addProperty("tag", "Image");
							dirArray.add(temp);

						}
					}
				}
			}

			return dirArray;
		}

		return null;

	}

	/**
	 *
	 * @param path chemin du fichier json contenant l'arborescence
	 * @return un arbre composer de treenode repr�sentant l'arborescance des fichiers
	 */
	public DefaultMutableTreeNode setDirectoryTree(String path) {

		JsonParser jsonParser = new JsonParser();

		try {

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));

			Object obj = jsonParser.parse(reader);

			JsonObject jsonRoot = (JsonObject) obj;
			JsonArray dirArray = (JsonArray) jsonRoot.get("root");
			TaggedTreeNode temp = new TaggedTreeNode("Dossier racine");
			createTree(dirArray, temp);

			return temp;

		} catch (JsonParseException e) {

			JOptionPane.showMessageDialog(null, "The hierarchy json file cannot be open correctly");
			LogFileWritingHandler.handleException(e.getMessage(), e.getStackTrace());

		} catch (FileNotFoundException e) {

			JOptionPane.showMessageDialog(null,	"The hierarchy json file cannot be open/found.\nCheck the conf.properties");
			LogFileWritingHandler.handleException(e.getMessage(), e.getStackTrace());
		} catch (NullPointerException e) {

			JOptionPane.showMessageDialog(null,
					"The arborescence of the file cannot be create.\nCheck the hierarchy json file integrity or check the conf.properties");
			LogFileWritingHandler.handleException(e.getMessage(), e.getStackTrace());
		} catch (ClassCastException e) {

			JOptionPane.showMessageDialog(null, "The hierarchy json file cannot be open correctly");
			LogFileWritingHandler.handleException(e.getMessage(), e.getStackTrace());
		} 

		return null;

	}

	/**
	 *
	 * @param a le json array contenant le dossier et les sousdossier
	 * @param d le noeud parent de ce dossier
	 */
	public void createTree(JsonArray a, TaggedTreeNode d) {

		if (a.size() > 0)
			// pour chaque enfant on regarde si c'est un dossier ou une image
			for (JsonElement child : a) {

				if (!child.isJsonNull() && ((JsonObject) child).has("nameImage")) {

					TaggedTreeNode temp = new TaggedTreeNode(((JsonObject) child).get("nameImage").getAsString(),
							((JsonObject) child).get("tag").getAsString());
					d.add(temp);

				} else if (!child.isJsonNull()) {

					TaggedTreeNode temp = new TaggedTreeNode(((JsonObject) child).get("name").getAsString(),
							((JsonObject) child).get("tag").getAsString());
					d.add(temp);
					createTree((JsonArray) ((JsonObject) child).get("nextDir"), temp);
				}

			}

	}

}