package CoflCore.events;

import CoflCore.classes.Position;
import java.util.List;

public class OnHighlightBlocks {
    public final List<Position> positions;

    public OnHighlightBlocks(List<Position> positions) {this.positions = positions;}
}
