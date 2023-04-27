/*
 * JVSTM: a Java library for Software Transactional Memory
 * Copyright (C) 2005 INESC-ID Software Engineering Group
 * http://www.esw.inesc-id.pt
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Author's contact:
 * INESC-ID Software Engineering Group
 * Rua Alves Redol 9
 * 1000 - 029 Lisboa
 * Portugal
 */
package jvstm.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RedBlackTree<E extends Comparable<? super E>> implements Iterable<E> {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    public static final RedBlackTree EMPTY = new RedBlackTree(BLACK, null, null, null);

    private boolean color;
    private E elem;
    private RedBlackTree<E> left;
    private RedBlackTree<E> right;

    private RedBlackTree(boolean color, E elem, RedBlackTree<E> left, RedBlackTree<E> right) {
        this.color = color;
        this.elem = elem;
        this.left = left;
        this.right = right;
    }

    public int size() {
        if (this == EMPTY) {
            return 0;
        } else {
            return left.size() + right.size() + 1;
        }
    }

    public RedBlackTree<E> put(E elem) {
        RedBlackTree<E> result = buildTree(elem);
        result.color = BLACK;
        return result;
    }

    private RedBlackTree<E> buildTree(E elem) {
        if (this == EMPTY) {
            return new RedBlackTree<E>(RED, elem, EMPTY, EMPTY);
        } else {
            int cmp = elem.compareTo(this.elem);
            if (cmp < 0) {
                return lbalance(this.color, this.elem, this.left.buildTree(elem), this.right);
            } else if (cmp > 0) {
                return rbalance(this.color, this.elem, this.left, this.right.buildTree(elem));
            } else {
                return new RedBlackTree<E>(this.color, elem, this.left, this.right);
            }
        }
    }

    private RedBlackTree<E> lbalance(boolean color, E elem, RedBlackTree<E> left, RedBlackTree<E> right) {
        if (color == BLACK) {
            if ((left != EMPTY) && (left.color == RED)) {
                if (left.left.color == RED) {
                    return new RedBlackTree<E>(RED, left.elem,
                            new RedBlackTree<E>(BLACK, left.left.elem, left.left.left, left.left.right),
                            new RedBlackTree<E>(BLACK, elem, left.right, right));
                }

                if (left.right.color == RED) {
                    return new RedBlackTree<E>(RED, left.right.elem,
                            new RedBlackTree<E>(BLACK, left.elem, left.left, left.right.left),
                            new RedBlackTree<E>(BLACK, elem, left.right.right, right));
                }
            }
        }

        return new RedBlackTree<E>(color, elem, left, right);
    }

    private RedBlackTree<E> rbalance(boolean color, E elem, RedBlackTree<E> left, RedBlackTree<E> right) {
        if (color == BLACK) {
            if ((right != EMPTY) && (right.color == RED)) {
                if (right.left.color == RED) {
                    return new RedBlackTree<E>(RED, right.left.elem, new RedBlackTree<E>(BLACK, elem, left, right.left.left),
                            new RedBlackTree<E>(BLACK, right.elem, right.left.right, right.right));
                }

                if (right.right.color == RED) {
                    return new RedBlackTree<E>(RED, right.elem, new RedBlackTree<E>(BLACK, elem, left, right.left),
                            new RedBlackTree<E>(BLACK, right.right.elem, right.right.left, right.right.right));
                }
            }
        }

        return new RedBlackTree<E>(color, elem, left, right);
    }

    public boolean contains(E elem) {
        return getNode(elem) != null;
    }

    public E get(E elem) {
        RedBlackTree<E> node = getNode(elem);
        return (node == null) ? null : node.elem;
    }

    protected RedBlackTree<E> getNode(E elem) {
        RedBlackTree<E> iter = this;

        while (iter != EMPTY) {
            int cmp = elem.compareTo(iter.elem);
            if (cmp < 0) {
                iter = iter.left;
            } else if (cmp > 0) {
                iter = iter.right;
            } else {
                return iter;
            }
        }

        return null;
    }

    protected RedBlackTree<E> getNodeLowerBound(E elem) {
        RedBlackTree<E> iter = this;
        RedBlackTree<E> candidate = null;

        while (iter != EMPTY) {
            int cmp = elem.compareTo(iter.elem);
            if (cmp == 0) {
                return iter;
            } else if (cmp < 0) {
                candidate = iter;
                iter = iter.left;
            } else {
                iter = iter.right;
            }
        }

        return candidate;
    }

    protected RedBlackTree<E> getNodeUpperBound(E elem) {
        RedBlackTree<E> iter = this;
        RedBlackTree<E> candidate = null;

        while (iter != EMPTY) {
            int cmp = elem.compareTo(iter.elem);
            if (cmp == 0) {
                return iter;
            } else if (cmp < 0) {
                iter = iter.left;
            } else {
                candidate = iter;
                iter = iter.right;
            }
        }

        return candidate;
    }

    public Iterator<E> iterator() {
        return new RBTIterator<E>(this);
    }

    private static final Iterator EMPTY_ITERATOR = new Iterator() {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    public Iterator<E> iterator(E from, E to) {
        RedBlackTree<E> lower = getNodeLowerBound(from);
        RedBlackTree<E> upper = getNodeUpperBound(to);

        if ((lower == upper) || (lower == null) || (upper == null) || (lower.elem.compareTo(upper.elem) > 0)) {
            return EMPTY_ITERATOR;
        }

        return new BoundedRBTIterator<E>(this, lower, upper);
    }

    static class RBTIterator<T extends Comparable<? super T>> implements Iterator<T> {
        protected Cons<RedBlackTree<T>> path;
        protected RedBlackTree<T> next;

        RBTIterator() {
            this.path = Cons.empty();
        }

        RBTIterator(RedBlackTree<T> root) {
            this();
            if (root != EMPTY) {
                findLeftmost(root);
            }
        }

        private void findLeftmost(RedBlackTree<T> node) {
            while (node.left != EMPTY) {
                path = path.cons(node);
                node = node.left;
            }
            this.next = node;
        }

        public boolean hasNext() {
            return next != null;
        }

        public T next() {
            if (next == null) {
                throw new NoSuchElementException();
            } else {
                T result = next.elem;

                if (next.right != EMPTY) {
                    findLeftmost(next.right);
                } else {
                    // no elements to the right, so climb up the tree
                    if (path == Cons.EMPTY) {
                        next = null;
                    } else {
                        next = path.first;
                        path = path.rest;
                    }
                }

                return result;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static class BoundedRBTIterator<T extends Comparable<? super T>> extends RBTIterator<T> {
        private RedBlackTree<T> last;

        BoundedRBTIterator(RedBlackTree<T> root, RedBlackTree<T> from, RedBlackTree<T> to) {
            super();
            this.last = to;

            findStart(root, from);
        }

        private void findStart(RedBlackTree<T> node, RedBlackTree<T> target) {
            while (node != target) {
                path = path.cons(node);
                int cmp = target.elem.compareTo(node.elem);
                if (cmp < 0) {
                    node = node.left;
                } else {
                    node = node.right;
                }
            }
            this.next = node;
        }

        public T next() {
            RedBlackTree<T> current = next;
            T result = super.next();
            if (current == last) {
                next = null;
            }
            return result;
        }
    }
}
