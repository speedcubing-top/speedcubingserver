package top.speedcubing.server.authenticator;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import javax.imageio.ImageIO;
import org.bukkit.entity.Player;
import top.speedcubing.server.player.User;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ImageRenderer extends MapRenderer {
    private final SoftReference<BufferedImage> cacheImage;
    private boolean hasRendered = false;

    public ImageRenderer(String url) throws IOException {
        this.cacheImage = new SoftReference<>(this.getImage(url));
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
        if (this.hasRendered) {
            return;
        }

        if (this.cacheImage.get() != null) {
            canvas.drawImage(0, 0, this.cacheImage.get());
        } else {
            User.getUser(player).sendMessage("%lang_server_image_render_failed%");
        }
        this.hasRendered = true;
    }

    private BufferedImage getImage(String url) throws IOException {
        boolean useCache = ImageIO.getUseCache();
        ImageIO.setUseCache(false);
        BufferedImage image = resize(new URL(url), new Dimension(128, 128));
        ImageIO.setUseCache(useCache);
        return image;
    }

    private BufferedImage resize(final URL url, final Dimension size) throws IOException {
        final BufferedImage image = ImageIO.read(url);
        final BufferedImage resized = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, size.width, size.height, null);
        g.dispose();
        return resized;
    }

}
