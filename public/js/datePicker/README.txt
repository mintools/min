Transcendent Date Selector
Author: Chris Stormer - chrisstormer.com
When you include the snipet of HTML anywhere inside your form tag,
the following variables will be passed to your php script in the global POST array:

$_POST['start_date'];
$_POST['end_date'];

or 

$HTTP_POST_VARS['start_date'];
$HTTP_POST_VARS['end_date'];

depending on your server configuration.

$start_date = $_POST['start_date'];
$end_date = $_POST['end_date']; 