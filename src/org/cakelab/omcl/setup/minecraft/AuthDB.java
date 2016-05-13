package org.cakelab.omcl.setup.minecraft;

import java.util.Iterator;

import org.cakelab.json.JSONObject;

public class AuthDB implements Iterable<AuthDB.Entry> {
	public class EntryIterator<T> implements Iterator<Entry> {


		private Iterator<java.util.Map.Entry<String, Object>> it;

		public EntryIterator() {
			it = AuthDB.this.content.entrySet().iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Entry next() {
			java.util.Map.Entry<String, Object> next = it.next();
			return new Entry(next.getKey(), (JSONObject)next.getValue());
		}

		@Override
		public void remove() {
			it.remove();
		}

	}

	public static class Entry {
		private String id;
		private JSONObject data;

		public Entry(String id, JSONObject data) {
			this.id = id;
			this.data = data;
		}

		public String getDisplayName() {
			return data == null ? null : data.getString("displayName");
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry other = (Entry) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

		public String getUsername() {
			return data == null ? null : data.getString("username");
		}

		public boolean isDemoAccount() {
			return id.startsWith("demo-");
		}

		public String getID() {
			return id;
		}

		
		
	}

	private JSONObject content;

	public AuthDB(JSONObject content) {
		this.content = content;
	}

	@Override
	public Iterator<Entry> iterator() {
		return new EntryIterator<Entry>();
	}

	public Entry get(String id) {
		JSONObject data = (JSONObject)content.get(id);
		return data == null ? null : new AuthDB.Entry(id, data);
	}

}
