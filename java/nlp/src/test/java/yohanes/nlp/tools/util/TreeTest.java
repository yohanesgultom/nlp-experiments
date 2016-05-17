package yohanes.nlp.tools.util;

import org.junit.Test;
import yohanes.nlp.tools.util.Tree;

import java.util.Arrays;

import static org.junit.Assert.*;
/**
 * Created by yohanes on 12/04/16.
 */
public class TreeTest {

    @Test
    public void testTreeCreation() {
        Tree tree = new Tree();
        assertEquals("", tree.toString());
        tree = new Tree("N");
        assertEquals("N", tree.toString());
        tree = new Tree("S", Arrays.asList(new Tree("Jeruk")));
        assertEquals("(S Jeruk)", tree.toString());
        tree = new Tree("S", Arrays.asList(
                new Tree("A", Arrays.asList(new Tree("Anggur"))),
                new Tree("B", Arrays.asList(
                        new Tree("C", Arrays.asList(new Tree("Cempedak"))),
                        new Tree("D", Arrays.asList(new Tree("Durian")))))));
        assertEquals("(S (A Anggur) (B (C Cempedak) (D Durian)))", tree.toString());
    }

    @Test
    public void testSetAndAdd() {
        Tree tree = new Tree();
        tree.setValue("Apel");
        assertEquals("Apel", tree.toString());
        tree = new Tree("S");
        tree.addChild(new Tree("Jeruk"));
        assertEquals("(S Jeruk)", tree.toString());
        tree = new Tree("S");
        tree.addChild(new Tree("A"));
        tree.addChild(new Tree("B"));
        tree.getChildren().get(0).addChild(new Tree("Anggur"));
        tree.getChildren().get(1).addChild(new Tree("C"));
        tree.getChildren().get(1).getChildren().get(0).addChild(new Tree("Cempedak"));
        assertEquals("(S (A Anggur) (B (C Cempedak)))", tree.toString());
    }

    @Test
    public void testParse() {
        Tree tree = Tree.parse("(NP  (NN\t  (Kera)) (SBAR  (SC\t  (untuk)) (S  (NP-SBJ  (*)) (VP  (VB\t  (amankan)) (NP  (NN\t  (pesta olahraga)))))))");
        String expected = "(NP (NN Kera) (SBAR (SC untuk) (S (NP-SBJ *) (VP (VB amankan) (NP (NN (NN pesta) (NN olahraga)))))))";
        assertEquals(expected, tree.toString());
    }
}
