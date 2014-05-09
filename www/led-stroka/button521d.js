jQuery(function($){
  if ($.browser.msie && ($.browser.version == '6.0' || $.browser.version == '7.0'))
		return;
  usernoiseButton.button = new usernoise.UsernoiseButton();
  usernoise.window = {
  	show: usernoiseButton.button.showWindow
  };
  $('.un-feedback-form').each(function(){
    new usernoise.FeedbackForm($(this));
  });
  if (usernoiseButton.showButton){
  	usernoiseButton.button.show();
	}
	$(document).bind('sent#feedbackform#window.un', function(){
	  var $overlay = $('<div id="un-overlay" />').appendTo($('body'));
	  function closeOverlay(){
	    $('#un-thankyou').find('a').unbind('click');
	    $('#un-thankyou').fadeOut('fast', function(){
	      $('#un-overlay').fadeOut('fast', function(){
          $('#un-thankyou').remove();
          $('#un-overlay').remove();
        });  
	    });
	    
	  }
	  $('#un-overlay').click(closeOverlay).fadeIn('fast', function(){
	    $('#un-thankyou').fadeIn('fast', function(){
	      setTimeout(closeOverlay, 5000);
	    }).find('#un-feedback-close').click(closeOverlay);
	  });
	});
	try{
    $('#' + usernoiseButton.custom_button_id).click(function(){usernoise.window.show(); return false;})
  } catch (err){
    alert('It looks like you entere wrong HTML ID value for custom Usernoise feedback button.');
  }
  $('a[rel=usernoise]').click(function(){usernoise.window.show(); return false;})
});