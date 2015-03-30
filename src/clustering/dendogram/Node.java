package clustering.dendogram;

import java.util.List;

public interface Node {
	Node getFirst();
	Node getSecond();
	int getIndex();
	List<String> getChildren();
}
