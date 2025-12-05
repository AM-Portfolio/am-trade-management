"""
Pagination helpers for SDK models.
"""

from typing import Any, Dict, Generic, List, Type, TypeVar

T = TypeVar('T')

class PagedResponse(Generic[T]):
    """
    Wrapper for paginated responses to return list of Models.
    """
    
    def __init__(self, data: Dict[str, Any], model_class: Type[T]):
        """
        Initialize PagedResponse.
        
        Args:
            data: Raw API response with 'content' list and page metadata
            model_class: The class to wrap each item in 'content' with
        """
        self.page_number = data.get("page_number", 0)
        self.page_size = data.get("page_size", 20)
        self.total_elements = data.get("total_elements", 0)
        self.total_pages = data.get("total_pages", 0)
        self.is_last = data.get("is_last", True)
        self.is_first = data.get("is_first", True)
        
        self.items: List[T] = [
            model_class(item) for item in data.get("content", [])
        ]
        
    def __iter__(self):
        """Allow iterating over items directly."""
        return iter(self.items)
    
    def __getitem__(self, index):
        """Allow indexing items directly."""
        return self.items[index]
        
    def __len__(self):
        """Return number of items in this page."""
        return len(self.items)

    def __repr__(self):
        return (
            f"<PagedResponse page={self.page_number}/{self.total_pages} "
            f"items={len(self.items)} total={self.total_elements}>"
        )
