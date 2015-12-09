package nars.guifx.treemap;

/**
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 */
class TreemapDtoElement {

    private double left;
    private double top;
    private double width;
    private double height;
    private double area;
    private final String label;
    private final Item item;

    public TreemapDtoElement(Item item) {
        area = item.getSize();
        label = item.getLabel();
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public double getArea() {
        return area;
    }

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "TreemapDtoElement{" +
                "label='" + label + '\'' +
                ", area=" + area +
                ", top=" + top +
                ", left=" + left +
                '}';
    }

    void setArea(double area) {
        this.area = area;
    }

    boolean isContainer() {
        return item.isContainer();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreemapDtoElement that = (TreemapDtoElement) o;

        if (item != null ? !item.equals(that.item) : that.item != null) return false;
        return !(label != null ? !label.equals(that.label) : that.label != null);

    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        return result;
    }
}
