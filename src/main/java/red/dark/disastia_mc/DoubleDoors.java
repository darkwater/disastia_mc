package red.dark.disastia_mc;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class DoubleDoors implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void openDoubleDoors(PlayerInteractEvent ev) {
        Block block = ev.getClickedBlock();
        BlockData blockData = block.getBlockData();

        if (!(blockData instanceof Door))
            return;

        Door doorData = (Door) blockData;
        boolean isOpen = doorData.isOpen();

        attemptSetOpenDoor(!isOpen, block, BlockFace.NORTH);
        attemptSetOpenDoor(!isOpen, block, BlockFace.EAST);
        attemptSetOpenDoor(!isOpen, block, BlockFace.SOUTH);
        attemptSetOpenDoor(!isOpen, block, BlockFace.WEST);
    }

    private void attemptSetOpenDoor(boolean open, Block block, BlockFace face) {
        Block neighbour = block.getRelative(face);
        BlockData blockData = neighbour.getBlockData();

        if (!(blockData instanceof Door))
            return;

        Door doorData = (Door) blockData;
        doorData.setOpen(open);
        neighbour.setBlockData(doorData);
    }
}
