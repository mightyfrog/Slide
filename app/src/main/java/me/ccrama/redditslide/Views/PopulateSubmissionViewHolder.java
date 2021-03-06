package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.ApiException;
import net.dean.jraw.fluent.FlairReference;
import net.dean.jraw.fluent.FluentRedditClient;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditOverview;
import me.ccrama.redditslide.Activities.SubredditOverviewSingle;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Adapters.SubmissionAdapter;
import me.ccrama.redditslide.Adapters.SubmissionViewHolder;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Vote;

/**
 * Created by ccrama on 9/19/2015.
 */
public class PopulateSubmissionViewHolder {


    private static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static int getStyleAttribColorValue(final Context context, final int attribResId, final int defaultValue) {
        final TypedValue tv = new TypedValue();
        final boolean found = context.getTheme().resolveAttribute(attribResId, tv, true);
        return found ? tv.data : defaultValue;
    }

    private static void addClickFunctions(final View base, final View clickingArea, ContentType.ImageType type, final Activity contextActivity, final Submission submission, final View back) {
        switch (type) {
            case NSFW_IMAGE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openImage(contextActivity, submission);

                    }
                });
                break;
            case EMBEDDED:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        if (Reddit.video) {
                            String data = submission.getDataNode().get("media_embed").get("content").asText();
                            {
                                Intent i = new Intent(contextActivity, FullscreenVideo.class);
                                i.putExtra("html", data);
                                contextActivity.startActivity(i);
                            }
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                    }
                });
                break;
            case NSFW_GIF:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        openGif(false, contextActivity, submission);

                    }
                });
                break;
            case NSFW_GFY:

                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif(true, contextActivity, submission);

                    }
                });
                break;
            case REDDIT:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openRedditContent(submission.getUrl(), contextActivity);
                    }
                });
                break;
            case LINK:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                            builder.setStartAnimations(contextActivity, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(contextActivity, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(contextActivity, Uri.parse(submission.getUrl()));
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                    }
                });
                break;
            case IMAGE_LINK:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                            builder.setStartAnimations(contextActivity, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(contextActivity, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(contextActivity, Uri.parse(submission.getUrl()));
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                    }

                });
                break;
            case NSFW_LINK:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);
                            builder.setStartAnimations(contextActivity, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(contextActivity, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(contextActivity, Uri.parse(submission.getUrl()));
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                    }
                });
                break;
            case SELF:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        if (back != null) {
                            back.performClick();
                        }


                    }
                });
                break;
            case GFY:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif(true, contextActivity, submission);

                    }
                });
                break;
            case ALBUM:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        if (Reddit.album) {
                            Intent i = new Intent(contextActivity, Album.class);
                            i.putExtra("url", submission.getUrl());
                            contextActivity.startActivity(i);
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);

                        }

                    }
                });
                break;
            case IMAGE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openImage(contextActivity, submission);


                    }
                });
                break;
            case GIF:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif(false, contextActivity, submission);

                    }
                });
                break;
            case NONE_GFY:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif(true, contextActivity, submission);

                    }
                });
                break;
            case NONE_GIF:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openGif(false, contextActivity, submission);

                    }
                });
                break;

            case NONE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        if (back != null) {
                            back.performClick();
                        }
                    }
                });

                break;
            case NONE_IMAGE:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        openImage(contextActivity, submission);


                    }
                });
                break;
            case NONE_URL:
                base.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                            builder.setStartAnimations(contextActivity, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(contextActivity, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(contextActivity, Uri.parse(submission.getUrl()));
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                    }
                });
                break;
            case VIDEO:
                base.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Reddit.video) {
                            Intent intent = new Intent(contextActivity, FullscreenVideo.class);
                            intent.putExtra("html", submission.getUrl());
                            contextActivity.startActivity(intent);
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }

                    }
                });

        }
    }

    public static void openRedditContent(String url, Context c) {
        new OpenRedditLink(c, url);
    }

    private static boolean isBlurry(JsonNode s, Context mC, String title) {
        int pixesl = s.get("preview").get("images").get(0).get("source").get("width").asInt();
        float density = mC.getResources().getDisplayMetrics().density;
        float dp = pixesl / density;
        Configuration configuration = mC.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.

        return dp < screenWidthDp / 3;
    }

    public static void openImage(Activity contextActivity, Submission submission) {
        if (Reddit.image) {
            DataShare.sharedSubmission = submission;
            Intent myIntent = new Intent(contextActivity, FullscreenImage.class);
            myIntent.putExtra("url", ContentType.getFixedUrl(submission.getUrl()));
            contextActivity.startActivity(myIntent);
        } else {
            Reddit.defaultShare(ContentType.getFixedUrl(submission.getUrl()), contextActivity);
        }

    }

    public static void openGif(final boolean gfy, Activity contextActivity, Submission submission) {
        if (Reddit.gif) {
            DataShare.sharedSubmission = submission;

            Intent myIntent = new Intent(contextActivity, GifView.class);
            if (gfy) {
                myIntent.putExtra("url", "gfy" + submission.getUrl());
            } else {
                myIntent.putExtra("url", "" + submission.getUrl());

            }
            contextActivity.startActivity(myIntent);
            contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
        } else {
            Reddit.defaultShare(submission.getUrl(), contextActivity);

        }

    }

    public <T> void PopulateSubmissionViewHolder(final SubmissionViewHolder holder, final Submission submission, final Activity mContext, boolean fullscreen, boolean full, final ArrayList<T> posts, final RecyclerView recyclerview, final boolean same) {



        holder.title.setText(Html.fromHtml(submission.getTitle()));

        holder.info.setText(submission.getAuthor() + " " + TimeUtils.getTimeAgo(submission.getCreatedUtc().getTime(), mContext));


        holder.subreddit.setText(submission.getSubredditName());

        if (SubredditStorage.modOf != null && SubredditStorage.modOf.contains(submission.getSubredditName().toLowerCase())) {
            holder.itemView.findViewById(R.id.mod).setVisibility(View.VISIBLE);
            final Map<String, Integer> reports = submission.getUserReports();
            final Map<String, String> reports2 = submission.getModeratorReports();
            if (reports.size() + reports2.size() > 0) {
                ((ImageView) holder.itemView.findViewById(R.id.mod)).getDrawable().setColorFilter(mContext.getResources().getColor(R.color.md_red_300), PorterDuff.Mode.MULTIPLY);
            } else {
                ((ImageView) holder.itemView.findViewById(R.id.mod)).getDrawable().setColorFilter(getStyleAttribColorValue(mContext, R.attr.tint, Color.WHITE), PorterDuff.Mode.MULTIPLY);

            }

            holder.itemView.findViewById(R.id.mod).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = (mContext).getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.modmenu, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                    builder.setView(dialoglayout);
                    final Dialog d = builder.show();
                    dialoglayout.findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AsyncTask<Void, Void, ArrayList<String>>() {
                                @Override
                                protected ArrayList<String> doInBackground(Void... params) {

                                    ArrayList<String> finalReports = new ArrayList<>();
                                    for (String s : reports.keySet()) {
                                        finalReports.add("x" + reports.get(s) + " " + s);
                                    }
                                    for (String s : reports2.keySet()) {
                                        finalReports.add(s + ": " + reports2.get(s));
                                    }
                                    if (finalReports.isEmpty()) {
                                        finalReports.add("No reports");
                                    }
                                    return finalReports;
                                }

                                @Override
                                public void onPostExecute(ArrayList<String> data) {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_reports).setItems(data.toArray(new CharSequence[data.size()]),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).show();
                                }
                            }.execute();

                        }
                    });

                    dialoglayout.findViewById(R.id.approve).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_approve)
                                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog, int which) {

                                            new AsyncTask<Void, Void, Boolean>() {

                                                @Override
                                                public void onPostExecute(Boolean b) {
                                                    if (b) {
                                                        dialog.dismiss();
                                                        d.dismiss();
                                                        if (mContext instanceof ModQueue) {

                                                            final int pos = posts.indexOf(submission);
                                                            posts.remove(submission);

                                                            recyclerview.getAdapter().notifyItemRemoved(pos);
                                                            dialog.dismiss();
                                                        }
                                                        Snackbar.make(recyclerview, R.string.mod_approved, Snackbar.LENGTH_LONG).show();

                                                    } else {
                                                        new AlertDialogWrapper.Builder(mContext)
                                                                .setTitle(R.string.err_general)
                                                                .setMessage(R.string.err_retry_later).show();
                                                    }
                                                }

                                                @Override
                                                protected Boolean doInBackground(Void... params) {
                                                    try {
                                                        new ModerationManager(Authentication.reddit).approve(submission);
                                                    } catch (ApiException e) {
                                                        e.printStackTrace();
                                                        return false;

                                                    }
                                                    return true;
                                                }
                                            }.execute();

                                        }
                                    }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();

                        }
                    });
                    dialoglayout.findViewById(R.id.spam).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });
                    dialoglayout.findViewById(R.id.nsfw).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!submission.isNsfw()) {
                                new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_mark_nsfw)
                                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, int which) {
                                                new AsyncTask<Void, Void, Boolean>() {

                                                    @Override
                                                    public void onPostExecute(Boolean b) {
                                                        if (b) {
                                                            dialog.dismiss();

                                                        } else {
                                                            new AlertDialogWrapper.Builder(mContext)
                                                                    .setTitle(R.string.err_general)
                                                                    .setMessage(R.string.err_retry_later).show();
                                                        }
                                                    }

                                                    @Override
                                                    protected Boolean doInBackground(Void... params) {
                                                        try {
                                                            new ModerationManager(Authentication.reddit).setNsfw(submission, true);
                                                        } catch (ApiException e) {
                                                            e.printStackTrace();
                                                            return false;

                                                        }
                                                        return true;
                                                    }
                                                }.execute();
                                            }
                                        }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                            } else {
                                new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_remove_nsfw)
                                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, int which) {
                                                new AsyncTask<Void, Void, Boolean>() {

                                                    @Override
                                                    public void onPostExecute(Boolean b) {
                                                        if (b) {
                                                            dialog.dismiss();
                                                        } else {
                                                            new AlertDialogWrapper.Builder(mContext)
                                                                    .setTitle(R.string.err_general)
                                                                    .setMessage(R.string.err_retry_later).show();
                                                        }
                                                    }

                                                    @Override
                                                    protected Boolean doInBackground(Void... params) {
                                                        try {
                                                            new ModerationManager(Authentication.reddit).setNsfw(submission, false);
                                                        } catch (ApiException e) {
                                                            e.printStackTrace();
                                                            return false;

                                                        }
                                                        return true;
                                                    }
                                                }.execute();

                                            }
                                        }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.flair).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AsyncTask<Void, Void, ArrayList<String>>() {
                                String currentFlair;
                                List<FlairTemplate> flair;
                                String input;

                                @Override
                                protected ArrayList<String> doInBackground(Void... params) {
                                    FlairReference allFlairs = new FluentRedditClient(Authentication.reddit).subreddit(submission.getSubredditName()).flair();

                                    try {
                                        flair = allFlairs.options();
                                        currentFlair = allFlairs.current().getText();
                                        final ArrayList<String> finalFlairs = new ArrayList<>();
                                        for (FlairTemplate temp : flair) {
                                            finalFlairs.add(temp.getText());
                                        }
                                        ((Activity) mContext).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                new MaterialDialog.Builder(mContext).title(R.string.mod_flair_post).inputType(InputType.TYPE_CLASS_TEXT)
                                                        .input(mContext.getString(R.string.mod_flair_hint), "", new MaterialDialog.InputCallback() {
                                                            @Override
                                                            public void onInput(MaterialDialog dialog, CharSequence out) {
                                                                input = out.toString();
                                                            }
                                                        }).items(finalFlairs.toArray(new String[finalFlairs.size()])).itemsCallback(new MaterialDialog.ListCallback() {
                                                    @Override
                                                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                                        materialDialog.dismiss();
                                                        try {
                                                            new ModerationManager(Authentication.reddit).setFlair(submission.getSubredditName(), flair.get(finalFlairs.indexOf(currentFlair)), input, submission);
                                                        } catch (ApiException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).show();
                                            }
                                        });
                                        return finalFlairs;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        //sub probably has no flairs?
                                    }


                                    return null;
                                }

                                @Override
                                public void onPostExecute(final ArrayList<String> data) {

                                }
                            }.execute();

                        }
                    });
                    dialoglayout.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_remove)
                                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        String reason;
                                        String flair;

                                        @Override
                                        public void onClick(final DialogInterface dialog, int which) {
                                            new MaterialDialog.Builder(mContext)
                                                    .title(R.string.mod_remove_hint)
                                                    .input(mContext.getString(R.string.mod_remove_hint_msg), "", false, new MaterialDialog.InputCallback() {
                                                        @Override
                                                        public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                                            reason = charSequence.toString();
                                                        }
                                                    }).positiveText(R.string.misc_continue).onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                                    new MaterialDialog.Builder(mContext)
                                                            .title(R.string.mod_flair)
                                                            .content(R.string.mod_flair_desc)
                                                            .input(mContext.getString(R.string.mod_flair_hint), "", true, new MaterialDialog.InputCallback() {
                                                                @Override
                                                                public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                                                    flair = charSequence.toString();
                                                                }
                                                            }).positiveText(R.string.btn_remove).onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                                            new AsyncTask<Void, Void, Boolean>() {

                                                                @Override
                                                                public void onPostExecute(Boolean b) {
                                                                    if (b) {
                                                                        dialog.dismiss();
                                                                        d.dismiss();
                                                                        if (mContext instanceof ModQueue || mContext instanceof SubredditOverview || mContext instanceof SubredditOverviewSingle) {
                                                                            final int pos = posts.indexOf(submission);
                                                                            posts.remove(submission);

                                                                            recyclerview.getAdapter().notifyItemRemoved(pos);
                                                                        }

                                                                        Snackbar.make(recyclerview, R.string.mod_post_removed, Snackbar.LENGTH_LONG).show();
                                                                    } else {
                                                                        new AlertDialogWrapper.Builder(mContext)
                                                                                .setTitle(R.string.err_general)
                                                                                .setMessage(R.string.err_retry_later).show();
                                                                    }
                                                                }

                                                                @Override
                                                                protected Boolean doInBackground(Void... params) {
                                                                    try {
                                                                        new ModerationManager(Authentication.reddit).remove(submission, true);
                                                                        if (!flair.isEmpty()) {
                                                                            //todo   new ModerationManager(Authentication.reddit).setFlair(submission.getSubredditName(), new , flair);
                                                                        }
                                                                        new AccountManager(Authentication.reddit).reply(submission, reason);

                                                                        return true;
                                                                    } catch (ApiException e) {
                                                                        e.printStackTrace();
                                                                        return false;

                                                                    }

                                                                }
                                                            }.execute();
                                                        }
                                                    }).show();
                                                }
                                            }).show();

                                        }
                                    }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }
                    });
                    dialoglayout.findViewById(R.id.ban).setVisibility(View.GONE);


                }
            });
        } else {
            holder.itemView.findViewById(R.id.mod).setVisibility(View.GONE);
        }

        holder.itemView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setText(submission.getTitle());

                ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra("profile", submission.getAuthor());
                        mContext.startActivity(i);
                    }
                });

                dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, SubredditView.class);
                        i.putExtra("subreddit", submission.getSubredditName());
                        mContext.startActivity(i);
                    }
                });


                dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (submission.saved) {
                            ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_save);
                        } else {
                            ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);

                        }
                        new SubmissionAdapter.AsyncSave(holder.itemView).execute(submission);

                    }
                });
                if (submission.saved) {
                    ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);
                }
                dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String urlString = "https://reddit.com" + submission.getPermalink();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage("com.android.chrome"); //Force open in chrome so it doesn't open back in Slide
                        try {
                            mContext.startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            intent.setPackage(null);
                            mContext.startActivity(intent);
                        }
                    }
                });
                dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialogWrapper.Builder(mContext).setTitle(R.string.submission_share_title)
                                .setNegativeButton(R.string.submission_share_reddit, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Reddit.defaultShareText("http://reddit.com" + submission.getPermalink(), mContext);

                                    }
                                }).setPositiveButton(R.string.submission_share_content, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Reddit.defaultShareText(submission.getUrl(), mContext);

                            }
                        }).show();

                    }
                });
                if (!Authentication.isLoggedIn) {
                    dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);
                    dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                }
                title.setBackgroundColor(Pallete.getColor(submission.getSubredditName()));

                builder.setView(dialoglayout);
                final Dialog d = builder.show();
                if (!Reddit.hideButton) {
                    dialoglayout.findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final int pos = posts.indexOf(submission);
                            final T t = posts.get(pos);
                            posts.remove(submission);

                            recyclerview.getAdapter().notifyItemRemoved(pos);
                            d.dismiss();
                            Hidden.setHidden((Contribution) t);

                            Snackbar.make(recyclerview, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    posts.add(pos, t);
                                    recyclerview.getAdapter().notifyItemInserted(pos);
                                    Hidden.undoHidden((Contribution) t);
                                }
                            }).show();
                        }
                    });
                } else {
                    dialoglayout.findViewById(R.id.hide).setVisibility(View.GONE);
                }
            }
        });
        int score = submission.getScore();
        int commentCount = submission.getCommentCount();
        Resources res = mContext.getResources();
        holder.comments.setText(res.getQuantityString(R.plurals.submission_comment_count, commentCount, commentCount));
        if (submission.getSubredditName().equals("androidcirclejerk")) {
            holder.score.setText(score + " upDuARTes"); //Praise DuARTe
        } else {
            holder.score.setText(res.getQuantityString(R.plurals.submission_points, score, score));
        }

        final ImageView downvotebutton = (ImageView) holder.itemView.findViewById(R.id.downvote);
        final ImageView upvotebutton = (ImageView) holder.itemView.findViewById(R.id.upvote);
        if (Authentication.isLoggedIn && !submission.voted()) {
            if (submission.getVote() == VoteDirection.UPVOTE) {
                downvotebutton.clearColorFilter();

                submission.setVote(true);
                submission.setVoted(true);
                holder.score.setTextColor(mContext.getResources().getColor(R.color.md_orange_500));
                upvotebutton.setColorFilter(mContext.getResources().getColor(R.color.md_orange_500), PorterDuff.Mode.MULTIPLY);

            } else if (submission.getVote() == VoteDirection.DOWNVOTE) {
                holder.score.setTextColor(mContext.getResources().getColor(R.color.md_blue_500));
                downvotebutton.setColorFilter(mContext.getResources().getColor(R.color.md_blue_500), PorterDuff.Mode.MULTIPLY);
                upvotebutton.clearColorFilter();

                submission.setVote(false);
                submission.setVoted(true);
            } else {
                holder.score.setTextColor(holder.comments.getCurrentTextColor());
                downvotebutton.clearColorFilter();
                upvotebutton.clearColorFilter();
                submission.setVote(false);
                submission.setVoted(false);

            }
        }

        final ImageView hideButton = (ImageView) holder.itemView.findViewById(R.id.hide);

        if (hideButton != null) {
            if (Reddit.hideButton) {
                hideButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int pos = posts.indexOf(submission);
                        final T t = posts.get(pos);
                        posts.remove(submission);

                        recyclerview.getAdapter().notifyItemRemoved(pos);
                        Hidden.setHidden((Contribution) t);

                        Snackbar.make(recyclerview, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                posts.add(pos, t);
                                recyclerview.getAdapter().notifyItemInserted(pos);
                                Hidden.undoHidden((Contribution) t);
                            }
                        }).show();
                    }
                });
            } else {
                hideButton.setVisibility(View.GONE);
            }
        }


        ContentType.ImageType type = ContentType.getImageType(submission);

        String url = "";

        boolean big = true;
        final String subreddit = (same) ? "second" : "";

        SettingValues.InfoBar typ = SettingValues.InfoBar.valueOf(SettingValues.prefs.getString(subreddit + "infoBarTypeNew", SettingValues.infoBar.toString()).toUpperCase());
        if (typ == SettingValues.InfoBar.INFO_BAR || typ == SettingValues.InfoBar.THUMBNAIL) {
            big = false;
        }
        holder.thumbImage.setVisibility(View.VISIBLE);
        if (!full) {
            ((ImageView) holder.itemView.findViewById(R.id.thumbimage2)).setImageBitmap(null);
        }
        if (!(typ == SettingValues.InfoBar.NONE && !full)) {

            boolean bigAtEnd = false;
            if (submission.isNsfw() && !SettingValues.NSFWPreviews) {
                bigAtEnd = false;
                holder.thumbImage.setVisibility(View.GONE);
                holder.imageArea.setVisibility(View.GONE);
                holder.previewContent.setVisibility(View.VISIBLE);
            } else if (type == ContentType.ImageType.IMAGE) {
                url = ContentType.getFixedUrl(submission.getUrl());
                if (CreateCardView.getInfoBar(same) == SettingValues.InfoBar.THUMBNAIL && !full) {

                    ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, ((ImageView) holder.itemView.findViewById(R.id.thumbimage2)));

                } else if (big || fullscreen) {
                    ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, holder.leadImage);

                    holder.imageArea.setVisibility(View.VISIBLE);
                    holder.previewContent.setVisibility(View.GONE);
                    bigAtEnd = true;
                } else {

                    ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, holder.thumbImage);

                    holder.imageArea.setVisibility(View.GONE);
                    holder.previewContent.setVisibility(View.VISIBLE);
                    bigAtEnd = false;
                }
            } else if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height") && submission.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt() > 200) {

                boolean blurry = isBlurry(submission.getDataNode(), mContext, submission.getTitle());
                holder.leadImage.setMinimumHeight(submission.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt());
                url = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                if (CreateCardView.getInfoBar(same) == SettingValues.InfoBar.THUMBNAIL && !full) {
                    ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, ((ImageView) holder.itemView.findViewById(R.id.thumbimage2)));
                } else if ((big || fullscreen) && !blurry) {
                    ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, holder.leadImage);

                    holder.imageArea.setVisibility(View.VISIBLE);
                    holder.previewContent.setVisibility(View.GONE);
                    bigAtEnd = true;
                } else {
                    ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, holder.thumbImage);
                    holder.imageArea.setVisibility(View.GONE);
                    holder.previewContent.setVisibility(View.VISIBLE);
                    bigAtEnd = false;
                }
            } else if (submission.getThumbnail() != null && (submission.getThumbnailType() == Submission.ThumbnailType.URL || submission.getThumbnailType() == Submission.ThumbnailType.NSFW)) {
                holder.leadImage.setMinimumHeight(0);

                if ((SettingValues.NSFWPreviews && submission.getThumbnailType() == Submission.ThumbnailType.NSFW) || submission.getThumbnailType() == Submission.ThumbnailType.URL) {
                    bigAtEnd = false;
                    if (CreateCardView.getInfoBar(same) == SettingValues.InfoBar.THUMBNAIL && !full) {
                        ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, ((ImageView) holder.itemView.findViewById(R.id.thumbimage2)));
                    } else {
                        ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, holder.thumbImage);
                    }
                    holder.imageArea.setVisibility(View.GONE);
                    holder.previewContent.setVisibility(View.VISIBLE);
                } else {
                    bigAtEnd = false;
                    holder.thumbImage.setVisibility(View.GONE);
                    holder.imageArea.setVisibility(View.GONE);
                    holder.previewContent.setVisibility(View.VISIBLE);
                }
            } else {
                bigAtEnd = false;
                holder.thumbImage.setVisibility(View.GONE);
                holder.imageArea.setVisibility(View.GONE);
                holder.previewContent.setVisibility(View.VISIBLE);
            }


            if (bigAtEnd) {
                holder.thumbImage.setVisibility(View.GONE);
            }
            TextView title;
            TextView info;
            if (bigAtEnd) {
                title = holder.textImage;
                info = holder.subTextImage;
            } else {
                title = holder.contentTitle;
                info = holder.contentURL;
            }
            if (typ == SettingValues.InfoBar.THUMBNAIL && !full) {
                holder.itemView.findViewById(R.id.base2).setVisibility(View.GONE);
            } else if (!full) {
                holder.itemView.findViewById(R.id.thumbimage2).setVisibility(View.GONE);

            }
            title.setVisibility(View.VISIBLE);
            info.setVisibility(View.VISIBLE);

            switch (type) {
                case NSFW_IMAGE:
                    title.setText(R.string.type_nsfw_img);
                    break;

                case NSFW_GIF:
                case NSFW_GFY:
                    title.setText(R.string.type_nsfw_gif);
                    break;

                case REDDIT:
                    title.setText(R.string.type_reddit);
                    break;

                case LINK:
                case IMAGE_LINK:
                    title.setText(R.string.type_link);
                    break;

                case NSFW_LINK:
                    title.setText(R.string.type_nsfw_link);

                    break;
                case SELF:
                    title.setVisibility(View.GONE);
                    info.setVisibility(View.GONE);
                    if (!bigAtEnd)
                        holder.previewContent.setVisibility(View.GONE);
                    break;

                case ALBUM:
                    title.setText(R.string.type_album);
                    break;

                case IMAGE:
                    if (submission.isNsfw() && !SettingValues.NSFWPreviews) {
                        title.setText(R.string.type_nsfw_img);

                    } else {
                        title.setVisibility(View.GONE);
                        info.setVisibility(View.GONE);
                    }
                    break;

                case GFY:
                case GIF:
                case NONE_GFY:
                case NONE_GIF:
                    title.setText(R.string.type_gif);
                    break;

                case NONE:
                    title.setText(R.string.type_title_only);
                    break;

                case NONE_IMAGE:
                    title.setText(R.string.type_img);
                    break;

                case VIDEO:
                    title.setText(R.string.type_vid);
                    break;

                case EMBEDDED:
                    title.setText(R.string.type_emb);
                    break;

                case NONE_URL:
                    title.setText(R.string.type_link);
                    break;
            }
            View baseView;

            View back = holder.itemView;

            try {
                info.setText(getDomainName(submission.getUrl()));
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
            if (bigAtEnd) {
                baseView = holder.imageArea;
            } else {
                baseView = holder.previewContent;
            }
            addClickFunctions(holder.imageArea, baseView, type, mContext, submission, back);
            addClickFunctions(holder.thumbImage, baseView, type, mContext, submission, back);
            addClickFunctions(holder.leadImage, baseView, type, mContext, submission, back);
            if (!full)
                addClickFunctions(holder.itemView.findViewById(R.id.thumbimage2), baseView, type, (Activity) mContext, submission, back);

            addClickFunctions(holder.previewContent, baseView, type, (Activity) mContext, submission, back);
        } else {
            holder.imageArea.setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.base2).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.thumbimage2).setVisibility(View.GONE);

        }

        View pinned = holder.itemView.findViewById(R.id.pinned);

        if (fullscreen) {
            View flair = holder.itemView.findViewById(R.id.flairbubble);

            if (submission.getSubmissionFlair().getText() == null || submission.getSubmissionFlair() == null || submission.getSubmissionFlair().getText().isEmpty() || submission.getSubmissionFlair().getText() == null) {
                flair.setVisibility(View.GONE);
            } else {
                flair.setVisibility(View.VISIBLE);
                Log.v("Slide", "FLAIR IS '" + submission.getSubmissionFlair().getText() + "'");
                ((TextView) flair.findViewById(R.id.text)).setText(submission.getSubmissionFlair().getText());
            }

            ActiveTextView bod = ((ActiveTextView) holder.itemView.findViewById(R.id.body));
            if (!submission.getSelftext().isEmpty()) {
                new MakeTextviewClickable().ParseTextWithLinksTextView(submission.getDataNode().get("selftext_html").asText(), bod, (Activity) mContext, submission.getSubredditName());
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.GONE);
            }
        }
        View nsfw = holder.itemView.findViewById(R.id.nsfw);
        if (submission.isStickied()) {
            pinned.setVisibility(View.VISIBLE);
        } else {
            pinned.setVisibility(View.GONE);
        }
        if (submission.isNsfw()) {
            nsfw.setVisibility(View.VISIBLE);
        } else {
            nsfw.setVisibility(View.GONE);

        }


        try {


            final TextView points = holder.score;
            final TextView comments = holder.comments;
            if (Authentication.isLoggedIn) {
                {
                    downvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!submission.voted()) {
                                points.setTextColor(mContext.getResources().getColor(R.color.md_blue_500));

                                submission.setVote(false);
                                downvotebutton.setColorFilter(mContext.getResources().getColor(R.color.md_blue_500), PorterDuff.Mode.MULTIPLY);
                                submission.setVoted(true);

                                new Vote(false, points, mContext).execute(submission);
                            } else if (submission.voted() && submission.getIsUpvoted()) {
                                new Vote(false, points, mContext).execute(submission);
                                points.setTextColor(mContext.getResources().getColor(R.color.md_blue_500));
                                downvotebutton.setColorFilter(mContext.getResources().getColor(R.color.md_blue_500), PorterDuff.Mode.MULTIPLY);
                                upvotebutton.clearColorFilter();
                                submission.setVoted(true);
                                submission.setVote(false);

                            } else if (submission.voted() && !submission.getIsUpvoted()) {
                                new Vote(points, mContext).execute(submission);
                                points.setTextColor(comments.getCurrentTextColor());
                                downvotebutton.clearColorFilter();
                                submission.setVoted(false);
                                submission.setVote(false);

                            }
                        }
                    });
                }
                {
                    upvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!submission.voted()) {
                                upvotebutton.setColorFilter(mContext.getResources().getColor(R.color.md_orange_500), PorterDuff.Mode.MULTIPLY);
                                submission.setVote(true);
                                submission.setVoted(true);

                                new Vote(true, points, mContext).execute(submission);
                                points.setTextColor(mContext.getResources().getColor(R.color.md_orange_500));
                            } else if (submission.voted() && !submission.getIsUpvoted()) {
                                new Vote(true, points, mContext).execute(submission);
                                points.setTextColor(mContext.getResources().getColor(R.color.md_orange_500));
                                submission.setVote(true);
                                submission.setVoted(true);

                                upvotebutton.setColorFilter(mContext.getResources().getColor(R.color.md_orange_500), PorterDuff.Mode.MULTIPLY);
                                downvotebutton.clearColorFilter();

                            } else if (submission.voted() && submission.getIsUpvoted()) {
                                points.setTextColor(comments.getCurrentTextColor());
                                new Vote(points, mContext).execute(submission);
                                submission.setVote(false);
                                submission.setVoted(false);

                                upvotebutton.clearColorFilter();

                            }
                        }
                    });
                }
            } else {
                upvotebutton.setVisibility(View.GONE);
                downvotebutton.setVisibility(View.GONE);

            }


        } catch (Exception ignored) {

        }
        if (HasSeen.getSeen(submission.getFullName()) && !full) {
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

    }

}
