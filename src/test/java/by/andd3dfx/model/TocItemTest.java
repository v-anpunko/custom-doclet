package by.andd3dfx.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import by.andd3dfx.model.TocItem.Builder;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class TocItemTest {

    private TocItem rootItem;

    @Before
    public void setup() {
        rootItem = prepareTocItem();
    }

    @Test
    public void testToString() {
        String result = rootItem.toString();

        assertThat("Wrong result", result, is(""
            + "- uid: root uid 1\n"
            + "  name: root name 1\n"
            + "  href: root href 1\n"
            + "  items: \n"
            + "  - uid: inner uid 1\n"
            + "    name: inner name 1\n"
            + "    href: inner href 1\n"
            + "  - uid: inner uid 2\n"
            + "    name: inner name 2\n"
            + "    href: inner href 2\n"
            + "    items: \n"
            + "    - uid: inner uid 3\n"
            + "      name: inner name 3\n"
            + "      href: inner href 3\n"));
    }

    @Test
    public void buildItemChunk() {
        String result = rootItem.buildItemChunk(4);

        assertThat("Wrong result", result, is(""
            + "    - uid: root uid 1\n"
            + "      name: root name 1\n"
            + "      href: root href 1\n"
            + "      items: \n"
            + "      - uid: inner uid 1\n"
            + "        name: inner name 1\n"
            + "        href: inner href 1\n"
            + "      - uid: inner uid 2\n"
            + "        name: inner name 2\n"
            + "        href: inner href 2\n"
            + "        items: \n"
            + "        - uid: inner uid 3\n"
            + "          name: inner name 3\n"
            + "          href: inner href 3\n"));
    }

    @Test
    public void buildItemsChunk() {
        String result = rootItem.buildItemsChunk(2, rootItem.getItems());

        assertThat("Wrong result", result, is(""
            + "  items: \n"
            + "  - uid: inner uid 1\n"
            + "    name: inner name 1\n"
            + "    href: inner href 1\n"
            + "  - uid: inner uid 2\n"
            + "    name: inner name 2\n"
            + "    href: inner href 2\n"
            + "    items: \n"
            + "    - uid: inner uid 3\n"
            + "      name: inner name 3\n"
            + "      href: inner href 3\n"));
    }

    private TocItem prepareTocItem() {
        TocItem rootItem = new Builder().setUid("root uid 1").setName("root name 1").setHref("root href 1").build();
        TocItem innerItem1 = new Builder().setUid("inner uid 1").setName("inner name 1").setHref("inner href 1")
            .build();
        TocItem innerItem2 = new Builder().setUid("inner uid 2").setName("inner name 2").setHref("inner href 2")
            .build();
        TocItem innerItem3 = new Builder().setUid("inner uid 3").setName("inner name 3").setHref("inner href 3")
            .build();
        innerItem2.getItems().add(innerItem3);
        rootItem.getItems().addAll(Arrays.asList(innerItem1, innerItem2));
        return rootItem;
    }
}