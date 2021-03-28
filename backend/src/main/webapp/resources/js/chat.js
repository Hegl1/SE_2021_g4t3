// the primeFaces-context is required to access the DOM of the scrollable panels
var pfContext;

function scrollToBottom(scrollComponentWidget) {
	var scrollComponentContainer = document
			.getElementById(scrollComponentWidget.id);
	var scrollComponent = scrollComponentContainer.children[0];
	scrollComponent.scrollTop = scrollComponent.scrollHeight
			- scrollComponent.clientHeight;
}

function scrollToChatBottom() {
	scrollToBottom(pfContext('chatWindowWidget'));
}
function scrollToEventLogAndOnlineInfoBottom() {
	scrollToBottom(pfContext('eventLogwWidget'));
	scrollToBottom(pfContext('onlineInfoWidget'));
}