/* Grammar -- Automatically generated by TreeBuilder */

package de.uni_freiburg.informatik.ultimate.astbuilder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a grammar.
 */
public class Grammar {
    /**
     * The package name of this grammar.
     */
    String packageName;

    /**
     * The imports of this grammar.
     */
    ArrayList<String> imports;

    /**
     * The node table of this grammar.
     */
    HashMap<String,Node> nodeTable;

    /**
     * The constructor taking initial values.
     * @param packageName the package name of this grammar.
     * @param imports the imports of this grammar.
     * @param nodeTable the node table of this grammar.
     */
    public Grammar(String packageName, ArrayList<String> imports, HashMap<String,Node> nodeTable) {
        super();
        this.packageName = packageName;
        this.imports = imports;
        this.nodeTable = nodeTable;
    }

    /**
     * Returns a textual description of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Grammar").append('[');
        sb.append(packageName);
        sb.append(',').append(imports);
        sb.append(',').append(nodeTable);
        return sb.append(']').toString();
    }

    /**
     * Gets the package name of this grammar.
     * @return the package name of this grammar.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Gets the imports of this grammar.
     * @return the imports of this grammar.
     */
    public ArrayList<String> getImports() {
        return imports;
    }

    /**
     * Gets the node table of this grammar.
     * @return the node table of this grammar.
     */
    public HashMap<String,Node> getNodeTable() {
        return nodeTable;
    }
}
