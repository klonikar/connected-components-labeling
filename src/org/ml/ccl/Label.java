package org.ml.ccl;

public class Label {
    public int name;

    public Label Root;

    public int Rank;

    public Label(int Name)
    {
        this.name = Name;
        this.Root = this;
        this.Rank = 0;
    }

    public int getName() {
		return name;
	}

	public void setName(int name) {
		this.name = name;
	}

	public Label getRoot() {
		return Root;
	}

	public void setRoot(Label root) {
		Root = root;
	}

	public int getRank() {
		return Rank;
	}

	public void setRank(int rank) {
		Rank = rank;
	}

	Label GetRoot()
    {
        if (this.Root != this)
        {
            this.Root = this.Root.GetRoot();
        }

        return this.Root;
    }

    void Join(Label root2)
    {
        if (root2.Rank < this.Rank)//is the rank of Root2 less than that of Root1 ?
        {
            root2.Root = this;//yes! then Root1 is the parent of Root2 (since it has the higher rank)
        }
        else //rank of Root2 is greater than or equal to that of Root1
        {
            this.Root = root2;//make Root2 the parent
            if (this.Rank == root2.Rank)//both ranks are equal ?
            {
                root2.Rank++;//increment Root2, we need to reach a single root for the whole tree
            }
        }
    }
}
